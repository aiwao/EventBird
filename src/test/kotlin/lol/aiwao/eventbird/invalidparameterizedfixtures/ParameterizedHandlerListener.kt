package lol.aiwao.eventbird.invalidparameterizedfixtures

import lol.aiwao.eventbird.Event
import lol.aiwao.eventbird.EventHandler
import lol.aiwao.eventbird.EventListener

class ParameterizedEvent<T> : Event()

@EventListener
object ParameterizedHandlerListener {
    @EventHandler
    fun onEvent(event: ParameterizedEvent<String>) = Unit
}
