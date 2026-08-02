package com.github.aiwao.eventbird.fixtures.child

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener
import com.github.aiwao.eventbird.fixtures.HandlerCalls

class ChildEvent(val value: String) : Event()

@EventListener
class ChildAnnotatedListener {
    @EventHandler
    fun onChildEvent(event: ChildEvent) {
        HandlerCalls.values += "${event.value}:child"
    }
}
