package com.github.aiwao.eventbird.invalidabstractfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener
import com.github.aiwao.eventbird.Register

abstract class AbstractEvent : Event()

@Register
object AbstractHandlerListener : EventListener() {
    @EventHandler
    fun onEvent(event: AbstractEvent) = Unit
}
