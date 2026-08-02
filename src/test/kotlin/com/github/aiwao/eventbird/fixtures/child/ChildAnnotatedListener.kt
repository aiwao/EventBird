package com.github.aiwao.eventbird.fixtures.child

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener
import com.github.aiwao.eventbird.Register
import com.github.aiwao.eventbird.fixtures.HandlerCalls

class ChildEvent(val value: String) : Event()

@Register
class ChildAnnotatedListener : EventListener() {
    @EventHandler
    fun onChildEvent(event: ChildEvent) {
        HandlerCalls.values += "${event.value}:child"
    }
}
