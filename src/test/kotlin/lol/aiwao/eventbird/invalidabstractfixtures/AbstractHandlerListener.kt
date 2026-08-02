package lol.aiwao.eventbird.invalidabstractfixtures

import lol.aiwao.eventbird.Event
import lol.aiwao.eventbird.EventHandler
import lol.aiwao.eventbird.EventListener

abstract class AbstractEvent : Event()

@EventListener
object AbstractHandlerListener {
    @EventHandler
    fun onEvent(event: AbstractEvent) = Unit
}
