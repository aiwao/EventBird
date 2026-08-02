package io.github.aiwao.eventbird.fixtures.child

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register
import io.github.aiwao.eventbird.fixtures.HandlerCalls

class ChildEvent(val value: String) : Event()

@Register
class ChildAnnotatedListener : EventListener() {
    @EventHandler
    fun onChildEvent(event: ChildEvent) {
        HandlerCalls.values += "${event.value}:child"
    }
}
