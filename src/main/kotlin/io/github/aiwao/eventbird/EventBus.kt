package io.github.aiwao.eventbird

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

class EventBus {
    private val registeredEventHandlers =
        mutableMapOf<KClass<out Event>, MutableList<RegisteredEventHandler>>()
    private val listenerInstancesByClass =
        mutableMapOf<KClass<out EventListener>, EventListener>()

    @Volatile
    private var dispatchTables = emptyMap<Class<out Event>, DispatchTable>()

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

        rebuildDispatchTables()
    }

    fun call(event: Event) {
        val dispatchTable = dispatchTables[event.javaClass] ?: return
        val handlers = if (event.pre) dispatchTable.pre else dispatchTable.post

        for (handler in handlers) {
            if (!handler.receiver.isEnabled) continue
            handler.invoker.invoke(event)
        }
    }

    private fun registerListener(listenerClass: KClass<out EventListener>) {
        val handlers = listenerClass.memberFunctions
            .mapNotNull { function ->
                function.findAnnotation<EventHandler>()
                    ?.let { annotation ->
                        DiscoveredEventHandler(
                            function = function,
                            pre = annotation.pre,
                            post = annotation.post,
                            priority = annotation.priority,
                        )
                    }
            }
            .sortedBy { handler -> handler.function.name }

        val handlerTypes = handlers.associateWith { handler ->
            getEventType(handler.function)
        }
        val listenerInstance = listenerInstancesByClass.getOrPut(listenerClass) {
            createListenerInstance(listenerClass)
        }

        handlerTypes.forEach { (handler, eventType) ->
            handler.function.isAccessible = true

            val handlersForType =
                registeredEventHandlers.getOrPut(eventType, ::mutableListOf)
            val isAlreadyRegistered = handlersForType.any { registeredHandler ->
                registeredHandler.function == handler.function &&
                    registeredHandler.receiver === listenerInstance
            }
            if (!isAlreadyRegistered) {
                handlersForType += RegisteredEventHandler(
                    function = handler.function,
                    receiver = listenerInstance,
                    pre = handler.pre,
                    post = handler.post,
                    priority = handler.priority,
                    invoker = createInvoker(handler.function, listenerInstance),
                )
            }
        }
    }

    private fun rebuildDispatchTables() {
        dispatchTables = registeredEventHandlers.mapKeys { (eventType) ->
            eventType.java
        }.mapValues { (_, handlers) ->
            val sortedHandlers = handlers.sortedByDescending { handler ->
                handler.priority
            }

            DispatchTable(
                pre = sortedHandlers.filter(RegisteredEventHandler::pre).toTypedArray(),
                post = sortedHandlers.filter(RegisteredEventHandler::post).toTypedArray(),
            )
        }
    }

    private fun createInvoker(
        function: KFunction<*>,
        receiver: EventListener,
    ): EventHandlerInvoker {
        val methodHandle = createMethodHandle(function, receiver)
            ?: return EventHandlerInvoker { event -> function.call(receiver, event) }

        return EventHandlerInvoker { event -> methodHandle.invokeExact(event) }
    }

    private fun createMethodHandle(
        function: KFunction<*>,
        receiver: EventListener,
    ): MethodHandle? {
        val javaMethod = function.javaMethod ?: return null

        return try {
            val lookup = MethodHandles.privateLookupIn(
                javaMethod.declaringClass,
                MethodHandles.lookup(),
            )

            lookup.unreflect(javaMethod)
                .bindTo(receiver)
                .asType(
                    MethodType.methodType(
                        Void.TYPE,
                        Event::class.java,
                    ),
                )
        } catch (_: Exception) {
            null
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

    private data class RegisteredEventHandler(
        val function: KFunction<*>,
        val receiver: EventListener,
        val pre: Boolean,
        val post: Boolean,
        val priority: Int,
        val invoker: EventHandlerInvoker,
    )

    private data class DiscoveredEventHandler(
        val function: KFunction<*>,
        val pre: Boolean,
        val post: Boolean,
        val priority: Int,
    )

    private data class DispatchTable(
        val pre: Array<RegisteredEventHandler>,
        val post: Array<RegisteredEventHandler>,
    )

    private fun interface EventHandlerInvoker {
        fun invoke(event: Event)
    }
}
