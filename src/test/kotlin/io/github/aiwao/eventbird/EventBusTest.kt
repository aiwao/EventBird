package io.github.aiwao.eventbird

import io.github.aiwao.eventbird.fixtures.AnnotatedListener
import io.github.aiwao.eventbird.fixtures.DirectEvent
import io.github.aiwao.eventbird.fixtures.EmptyListener
import io.github.aiwao.eventbird.fixtures.HandlerCalls
import io.github.aiwao.eventbird.fixtures.ListenerWithoutAnnotation
import io.github.aiwao.eventbird.fixtures.ObjectAnnotatedListener
import io.github.aiwao.eventbird.fixtures.ObjectEvent
import io.github.aiwao.eventbird.fixtures.RegisteredInheritedListener
import io.github.aiwao.eventbird.fixtures.child.ChildEvent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        eventBus.register("io/github/aiwao/eventbird/fixtures")

        eventBus.call(DirectEvent("direct"))
        eventBus.call(ObjectEvent("object"))
        eventBus.call(ChildEvent("child"))

        assertEquals(
            listOf(
                "direct:first",
                "direct:second",
                "direct:inherited",
                "object:object",
                "child:child",
            ),
            HandlerCalls.values,
        )
        assertEquals(1, AnnotatedListener.createdInstances)
    }

    @Test
    fun `register does not add the same handler more than once`() {
        val eventBus = EventBus()

        eventBus.register("io.github.aiwao.eventbird.fixtures")
        eventBus.register("io.github.aiwao.eventbird.fixtures")
        eventBus.call(DirectEvent("once"))

        assertEquals(
            listOf("once:first", "once:second", "once:inherited"),
            HandlerCalls.values,
        )
        assertEquals(1, AnnotatedListener.createdInstances)
    }

    @Test
    fun `listener instances are accessible and reused`() {
        val eventBus = EventBus()

        eventBus.register("io.github.aiwao.eventbird.fixtures")
        val firstSnapshot = eventBus.listenerInstances
        eventBus.register("io.github.aiwao.eventbird.fixtures")
        val secondSnapshot = eventBus.listenerInstances

        assertEquals(5, firstSnapshot.size)
        assertEquals(5, secondSnapshot.size)
        firstSnapshot.zip(secondSnapshot).forEach { (first, second) ->
            assertSame(first, second)
        }
        assertTrue(ObjectAnnotatedListener in secondSnapshot)
        assertTrue(secondSnapshot.any { listener -> listener is EmptyListener })
        assertTrue(secondSnapshot.any { listener ->
            listener is RegisteredInheritedListener
        })
        assertFalse(secondSnapshot.any { listener ->
            listener is ListenerWithoutAnnotation
        })
    }

    @Test
    fun `call only invokes handlers of enabled listeners`() {
        val eventBus = EventBus()
        eventBus.register("io.github.aiwao.eventbird.fixtures")
        val listener = eventBus.listenerInstances
            .filterIsInstance<AnnotatedListener>()
            .single()

        listener.isEnabled = false
        eventBus.call(DirectEvent("disabled"))
        eventBus.call(ObjectEvent("enabled"))

        assertFalse(listener.isEnabled)
        assertEquals(
            listOf("disabled:inherited", "enabled:object"),
            HandlerCalls.values,
        )

        listener.isEnabled = true
        eventBus.call(DirectEvent("re-enabled"))

        assertEquals(
            listOf(
                "disabled:inherited",
                "enabled:object",
                "re-enabled:first",
                "re-enabled:second",
                "re-enabled:inherited",
            ),
            HandlerCalls.values,
        )
    }

    @Test
    fun `register rejects a generic event handler`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            EventBus().register("io.github.aiwao.eventbird.invalidgenericfixtures")
        }

        assertTrue(exception.message.orEmpty().contains("must not declare type parameters"))
    }

    @Test
    fun `register rejects a parameterized event type`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            EventBus().register("io.github.aiwao.eventbird.invalidparameterizedfixtures")
        }

        assertTrue(exception.message.orEmpty().contains("must not be a parameterized type"))
    }

    @Test
    fun `register rejects an abstract event type`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            EventBus().register("io.github.aiwao.eventbird.invalidabstractfixtures")
        }

        assertTrue(exception.message.orEmpty().contains("concrete Event type"))
    }

    @Test
    fun `register rejects a listener that cannot be instantiated`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            EventBus().register("io.github.aiwao.eventbird.invalidfixtures")
        }

        assertTrue(exception.message.orEmpty().contains("no-argument constructor"))
    }
}
