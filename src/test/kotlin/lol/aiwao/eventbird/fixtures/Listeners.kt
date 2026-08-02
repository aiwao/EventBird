package lol.aiwao.eventbird.fixtures

import lol.aiwao.eventbird.Event
import lol.aiwao.eventbird.EventHandler
import lol.aiwao.eventbird.EventListener

object HandlerCalls {
    val values = mutableListOf<String>()
}

class DirectEvent(val value: String) : Event()

class ObjectEvent(val value: String) : Event()

@EventListener
class AnnotatedListener {
    init {
        createdInstances++
    }

    @EventHandler
    fun first(event: DirectEvent) {
        HandlerCalls.values += "${event.value}:first"
    }

    @EventHandler
    fun second(event: DirectEvent) {
        HandlerCalls.values += "${event.value}:second"
    }

    fun functionWithoutAnnotation(event: DirectEvent) {
        HandlerCalls.values += "${event.value}:unannotated-function"
    }

    companion object {
        var createdInstances = 0
    }
}

@EventListener
object ObjectAnnotatedListener {
    @EventHandler
    fun onObjectEvent(event: ObjectEvent) {
        HandlerCalls.values += "${event.value}:object"
    }
}

class ListenerWithoutAnnotation {
    @EventHandler
    fun onDirectEvent(event: DirectEvent) {
        HandlerCalls.values += "${event.value}:unannotated-class"
    }
}
