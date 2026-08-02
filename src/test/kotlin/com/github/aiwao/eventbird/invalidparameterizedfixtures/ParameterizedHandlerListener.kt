package com.github.aiwao.eventbird.invalidparameterizedfixtures

import com.github.aiwao.eventbird.Event
import com.github.aiwao.eventbird.EventHandler
import com.github.aiwao.eventbird.EventListener

class ParameterizedEvent<T> : Event()

@EventListener
object ParameterizedHandlerListener {
    @EventHandler
    fun onEvent(event: ParameterizedEvent<String>) = Unit
}
