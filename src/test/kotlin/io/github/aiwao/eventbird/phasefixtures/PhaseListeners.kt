package io.github.aiwao.eventbird.phasefixtures

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register

object PhaseHandlerCalls {
    val values = mutableListOf<String>()
}

class PhaseEvent(
    val value: String,
    pre: Boolean,
) : Event(pre)

@Register
class PhaseListener : EventListener {
    override var isEnabled = false

    @EventHandler(pre = true, post = true)
    fun both(event: PhaseEvent) {
        PhaseHandlerCalls.values += "${event.value}:both"
    }

    @EventHandler(pre = false, post = false)
    fun neither(event: PhaseEvent) {
        PhaseHandlerCalls.values += "${event.value}:neither"
    }

    @EventHandler(pre = false, post = true)
    fun post(event: PhaseEvent) {
        PhaseHandlerCalls.values += "${event.value}:post"
    }

    @EventHandler(pre = true, post = false)
    fun pre(event: PhaseEvent) {
        PhaseHandlerCalls.values += "${event.value}:pre"
    }
}
