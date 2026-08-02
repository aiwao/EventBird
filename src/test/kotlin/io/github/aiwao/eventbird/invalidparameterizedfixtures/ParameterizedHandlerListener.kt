package io.github.aiwao.eventbird.invalidparameterizedfixtures

import io.github.aiwao.eventbird.Event
import io.github.aiwao.eventbird.EventHandler
import io.github.aiwao.eventbird.EventListener
import io.github.aiwao.eventbird.Register

class ParameterizedEvent<T> : Event()

@Register
object ParameterizedHandlerListener : EventListener() {
    @EventHandler
    fun onEvent(event: ParameterizedEvent<String>) = Unit
}
