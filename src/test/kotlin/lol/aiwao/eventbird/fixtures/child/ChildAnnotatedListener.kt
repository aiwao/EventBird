package lol.aiwao.eventbird.fixtures.child

import lol.aiwao.eventbird.Event
import lol.aiwao.eventbird.EventHandler
import lol.aiwao.eventbird.EventListener
import lol.aiwao.eventbird.fixtures.HandlerCalls

class ChildEvent(val value: String) : Event()

@EventListener
class ChildAnnotatedListener {
    @EventHandler
    fun onChildEvent(event: ChildEvent) {
        HandlerCalls.values += "${event.value}:child"
    }
}
