package io.github.aiwao.eventbird.fixtures

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register

object HandlerCalls {
    val values = mutableListOf<String>()
}

class DirectEvent(val value: String) : Event()

class ObjectEvent(val value: String) : Event()

@Register
class AnnotatedListener : EventListener() {
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

@Register
object ObjectAnnotatedListener : EventListener() {
    @EventHandler
    fun onObjectEvent(event: ObjectEvent) {
        HandlerCalls.values += "${event.value}:object"
    }
}

class ListenerWithoutAnnotation : EventListener() {
    @EventHandler
    fun onDirectEvent(event: DirectEvent) {
        HandlerCalls.values += "${event.value}:unannotated-class"
    }
}
