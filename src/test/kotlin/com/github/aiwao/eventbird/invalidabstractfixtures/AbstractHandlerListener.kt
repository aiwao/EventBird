package com.github.aiwao.eventbird.invalidabstractfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener

abstract class AbstractEvent : Event()

@EventListener
object AbstractHandlerListener {
    @EventHandler
    fun onEvent(event: AbstractEvent) = Unit
}
