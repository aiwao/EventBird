package com.github.aiwao.eventbird.invalidparameterizedfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener
import com.github.aiwao.eventbird.Register

class ParameterizedEvent<T> : Event()

@Register
object ParameterizedHandlerListener : EventListener() {
    @EventHandler
    fun onEvent(event: ParameterizedEvent<String>) = Unit
}
