package lol.aiwao.eventbird

import lol.aiwao.eventbird.fixtures.AnnotatedListener
import lol.aiwao.eventbird.fixtures.DirectEvent
import lol.aiwao.eventbird.fixtures.GenericAnnotatedListener
import lol.aiwao.eventbird.fixtures.GenericEvent
import lol.aiwao.eventbird.fixtures.HandlerCalls
import lol.aiwao.eventbird.fixtures.child.ChildEvent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EventBusTest {
    @BeforeTest
    fun clearHandlerCalls() {
        HandlerCalls.values.clear()
        AnnotatedListener.createdInstances = 0
    }

    @Test
    fun `call invokes all handlers registered for the event type`() {
        val eventBus = EventBus()
        eventBus.register("lol/aiwao/eventbird/fixtures")

        eventBus.call(DirectEvent("direct"))
        eventBus.call(GenericEvent("generic"))
        eventBus.call(ChildEvent("child"))

        assertEquals(
            listOf(
                "direct:first",
                "direct:second",
                "generic:generic",
                "child:child",
            ),
            HandlerCalls.values,
        )
        assertEquals(1, AnnotatedListener.createdInstances)
    }

    @Test
    fun `register does not add the same handler more than once`() {
        val eventBus = EventBus()

        eventBus.register("lol.aiwao.eventbird.fixtures")
        eventBus.register("lol.aiwao.eventbird.fixtures")
        eventBus.call(DirectEvent("once"))

        assertEquals(listOf("once:first", "once:second"), HandlerCalls.values)
        assertEquals(1, AnnotatedListener.createdInstances)
    }

    @Test
    fun `listener instances are accessible and reused`() {
        val eventBus = EventBus()

        eventBus.register("lol.aiwao.eventbird.fixtures")
        val firstSnapshot = eventBus.listenerInstances
        eventBus.register("lol.aiwao.eventbird.fixtures")
        val secondSnapshot = eventBus.listenerInstances

        assertEquals(3, firstSnapshot.size)
        assertEquals(3, secondSnapshot.size)
        firstSnapshot.zip(secondSnapshot).forEach { (first, second) ->
            assertSame(first, second)
        }
        assertTrue(GenericAnnotatedListener in secondSnapshot)
    }

    @Test
    fun `register rejects a listener that cannot be instantiated`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            EventBus().register("lol.aiwao.eventbird.invalidfixtures")
        }

        assertTrue(exception.message.orEmpty().contains("no-argument constructor"))
    }
}
