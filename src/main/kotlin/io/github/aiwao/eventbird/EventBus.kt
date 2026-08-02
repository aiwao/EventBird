package io.github.aiwao.eventbird

import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

class EventBus {
    private val eventHandlers =
        mutableMapOf<KClass<out Event>, MutableList<KFunction<*>>>()
    private val listenerInstancesByClass =
        mutableMapOf<KClass<out EventListener>, EventListener>()
    private val handlerReceivers = mutableMapOf<KFunction<*>, EventListener>()

    /** Returns a snapshot of the listener instances held by this EventBus. */
    val listenerInstances: List<EventListener>
        get() = listenerInstancesByClass.values.toList()

    fun register(packagePath: String) {
        val packageName = normalizePackagePath(packagePath)

        Reflections(packageName)
            .getTypesAnnotatedWith(Register::class.java)
            .map { listenerClass -> listenerClass.kotlin.asEventListenerClass() }
            .sortedBy { listenerClass -> listenerClass.qualifiedName }
            .forEach(::registerListener)
    }

    fun call(event: Event) {
        eventHandlers[event::class]
            ?.toList()
            ?.forEach { handler ->
                val receiver = checkNotNull(handlerReceivers[handler]) {
                    "No listener instance is registered for $handler"
                }
                if (!receiver.isEnabled) return@forEach

                handler.call(receiver, event)
            }
    }

    private fun registerListener(listenerClass: KClass<out EventListener>) {
        val handlers = listenerClass.declaredMemberFunctions
            .filter { function -> function.findAnnotation<EventHandler>() != null }
            .sortedBy { function -> function.name }

        if (handlers.isEmpty()) return

        val handlerTypes = handlers.associateWith(::getEventType)
        val listenerInstance = listenerInstancesByClass.getOrPut(listenerClass) {
            createListenerInstance(listenerClass)
        }

        handlerTypes.forEach { (handler, eventType) ->
            handler.isAccessible = true
            handlerReceivers[handler] = listenerInstance

            val handlersForType = eventHandlers.getOrPut(eventType, ::mutableListOf)
            if (handler !in handlersForType) {
                handlersForType += handler
            }
        }
    }

    private fun getEventType(handler: KFunction<*>): KClass<out Event> {
        require(!handler.isSuspend) {
            "@EventHandler function must not be suspend: $handler"
        }
        require(handler.extensionReceiverParameter == null) {
            "@EventHandler function must not be an extension function: $handler"
        }
        require(handler.typeParameters.isEmpty()) {
            "@EventHandler function must not declare type parameters: $handler"
        }

        val eventParameter = handler.valueParameters.singleOrNull()
            ?: throw IllegalArgumentException(
                "@EventHandler function must have exactly one argument: $handler",
            )

        val eventType = eventParameter.type
        require(eventType.arguments.isEmpty()) {
            "@EventHandler argument must not be a parameterized type: $handler"
        }

        return (eventType.classifier as? KClass<*>)?.asConcreteEventClass()
            ?: throw IllegalArgumentException(
                "@EventHandler argument must be a concrete Event type: $handler",
            )
    }

    @Suppress("UNCHECKED_CAST")
    private fun KClass<*>.asConcreteEventClass(): KClass<out Event>? =
        takeIf { candidate ->
            !candidate.isAbstract &&
                Event::class.java.isAssignableFrom(candidate.java)
        }
            as? KClass<out Event>

    @Suppress("UNCHECKED_CAST")
    private fun KClass<*>.asEventListenerClass(): KClass<out EventListener> {
        require(EventListener::class.java.isAssignableFrom(java)) {
            "@Register class must extend EventListener: $qualifiedName"
        }

        return this as KClass<out EventListener>
    }

    private fun createListenerInstance(
        listenerClass: KClass<out EventListener>,
    ): EventListener =
        listenerClass.objectInstance ?: try {
            listenerClass.createInstance()
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "@Register class must be an object or have a no-argument " +
                    "constructor (all-default constructors are also supported): " +
                    listenerClass.qualifiedName,
                exception,
            )
        }

    private fun normalizePackagePath(packagePath: String): String {
        val packageName = packagePath
            .trim()
            .replace('\\', '/')
            .trim('/')
            .replace('/', '.')
            .trim('.')

        require(packageName.isNotEmpty()) { "packagePath must not be blank" }
        require(packageName.split('.').none(String::isEmpty)) {
            "packagePath must be a valid package path: $packagePath"
        }

        return packageName
    }
}
