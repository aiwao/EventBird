package com.github.aiwao.eventbird.invalidgenericfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener

open class GenericHandlerEvent : Event()

@EventListener
object GenericHandlerListener {
    @EventHandler
    fun <T : GenericHandlerEvent> onEvent(event: T) = Unit
}
