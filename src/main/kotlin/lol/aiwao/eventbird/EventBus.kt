package lol.aiwao.eventbird

import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

class EventBus {
    private val eventHandlers =
        mutableMapOf<KClass<out Event>, MutableList<KFunction<*>>>()
    private val listenerInstances = mutableMapOf<KClass<*>, Any>()
    private val handlerReceivers = mutableMapOf<KFunction<*>, Any>()

    fun register(packagePath: String) {
        val packageName = normalizePackagePath(packagePath)

        Reflections(packageName)
            .getTypesAnnotatedWith(EventListener::class.java)
            .map { listenerClass -> listenerClass.kotlin }
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

                handler.call(receiver, event)
            }
    }

    private fun registerListener(listenerClass: KClass<*>) {
        val handlers = listenerClass.declaredMemberFunctions
            .filter { function -> function.findAnnotation<EventHandler>() != null }
            .sortedBy { function -> function.name }

        if (handlers.isEmpty()) return

        val handlerTypes = handlers.associateWith(::getEventType)
        val listenerInstance = listenerInstances.getOrPut(listenerClass) {
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

        val eventParameter = handler.valueParameters.singleOrNull()
            ?: throw IllegalArgumentException(
                "@EventHandler function must have exactly one argument: $handler",
            )

        return resolveEventType(eventParameter.type)
            ?: throw IllegalArgumentException(
                "@EventHandler argument must be an Event type: $handler",
            )
    }

    private fun resolveEventType(
        type: KType,
        visitedTypeParameters: Set<KTypeParameter> = emptySet(),
    ): KClass<out Event>? = when (val classifier = type.classifier) {
        is KClass<*> -> classifier.asEventClass()

        is KTypeParameter -> {
            if (classifier in visitedTypeParameters) {
                null
            } else {
                classifier.upperBounds.firstNotNullOfOrNull { upperBound ->
                    resolveEventType(upperBound, visitedTypeParameters + classifier)
                }
            }
        }

        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    private fun KClass<*>.asEventClass(): KClass<out Event>? =
        takeIf { candidate -> Event::class.java.isAssignableFrom(candidate.java) }
            as? KClass<out Event>

    private fun createListenerInstance(listenerClass: KClass<*>): Any =
        listenerClass.objectInstance ?: try {
            listenerClass.createInstance()
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "@EventListener class must be an object or have a no-argument " +
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
