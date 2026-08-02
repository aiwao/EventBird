package io.github.aiwao.eventbird

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(
    val pre: Boolean = true,
    val post: Boolean = true,
    val priority: Int = 0,
)
