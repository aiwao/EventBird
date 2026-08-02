package lol.aiwao.eventbird.invalidfixtures

import lol.aiwao.eventbird.Event
import lol.aiwao.eventbird.EventHandler
import lol.aiwao.eventbird.EventListener

class InvalidEvent : Event()

@EventListener
class RequiredConstructorListener(private val dependency: String) {
    @EventHandler
    fun onInvalidEvent(event: InvalidEvent) {
        dependency.length + event.hashCode()
    }
}
