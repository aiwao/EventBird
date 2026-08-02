package com.github.aiwao.eventbird.invalidgenericfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener
import com.github.aiwao.eventbird.Register

open class GenericHandlerEvent : Event()

@Register
object GenericHandlerListener : EventListener() {
    @EventHandler
    fun <T : GenericHandlerEvent> onEvent(event: T) = Unit
}
