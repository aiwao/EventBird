package lol.aiwao.eventbird.invalidgenericfixtures

import lol.aiwao.eventbird.Event
import lol.aiwao.eventbird.EventHandler
import lol.aiwao.eventbird.EventListener

open class GenericHandlerEvent : Event()

@EventListener
object GenericHandlerListener {
    @EventHandler
    fun <T : GenericHandlerEvent> onEvent(event: T) = Unit
}
