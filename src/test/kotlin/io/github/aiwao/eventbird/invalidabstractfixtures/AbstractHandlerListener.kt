package io.github.aiwao.eventbird.invalidabstractfixtures

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register

abstract class AbstractEvent : Event()

@Register
object AbstractHandlerListener : EventListener() {
    @EventHandler
    fun onEvent(event: AbstractEvent) = Unit
}
