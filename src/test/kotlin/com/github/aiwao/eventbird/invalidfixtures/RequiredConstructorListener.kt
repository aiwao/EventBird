package com.github.aiwao.eventbird.invalidfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener
import com.github.aiwao.eventbird.Register

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
