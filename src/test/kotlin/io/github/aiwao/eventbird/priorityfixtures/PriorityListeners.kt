package io.github.aiwao.eventbird.priorityfixtures

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register

object PriorityHandlerCalls {
    val values = mutableListOf<String>()
}

class PriorityEvent : Event()

@Register
class MiddlePriorityListener : EventListener() {
    @EventHandler(priority = 50)
    fun middle(event: PriorityEvent) {
        PriorityHandlerCalls.values += "middle:${event.pre}"
    }
}

@Register
class PriorityListener : EventListener() {
    @EventHandler(priority = -100)
    fun aLow(event: PriorityEvent) {
        PriorityHandlerCalls.values += "low:${event.pre}"
    }

    @EventHandler
    fun bDefault(event: PriorityEvent) {
        PriorityHandlerCalls.values += "default:${event.pre}"
    }

    @EventHandler(priority = 100)
    fun cHigh(event: PriorityEvent) {
        PriorityHandlerCalls.values += "high:${event.pre}"
    }
}
