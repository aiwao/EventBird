package com.github.aiwao.eventbird.invalidfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener

class InvalidEvent : Event()

@EventListener
class RequiredConstructorListener(private val dependency: String) {
    @EventHandler
    fun onInvalidEvent(event: InvalidEvent) {
        dependency.length + event.hashCode()
    }
}
