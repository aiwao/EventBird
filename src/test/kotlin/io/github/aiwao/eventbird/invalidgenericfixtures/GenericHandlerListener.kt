package io.github.aiwao.eventbird.invalidgenericfixtures

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register

open class GenericHandlerEvent : Event()

@Register
object GenericHandlerListener : EventListener {
    override var isEnabled = false

    @EventHandler
    fun <T : GenericHandlerEvent> onEvent(event: T) = Unit
}
