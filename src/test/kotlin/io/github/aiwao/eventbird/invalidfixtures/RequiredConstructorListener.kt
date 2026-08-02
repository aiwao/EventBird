package io.github.aiwao.eventbird.invalidfixtures

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register

class InvalidEvent : Event()

@Register
class RequiredConstructorListener(
    private val dependency: String,
) : EventListener() {
    @EventHandler
    fun onInvalidEvent(event: InvalidEvent) {
        dependency.length + event.hashCode()
    }
}
