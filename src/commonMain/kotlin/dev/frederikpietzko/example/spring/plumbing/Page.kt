package dev.frederikpietzko.example.spring.plumbing

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

interface PageStateProvider<State> {
    fun provide(): State
}

object NoState : PageStateProvider<NoState> {
    override fun provide(): NoState = NoState
}

data class PageDefinition<State>(
    val path: String,
    val stateProvider: KClass<out PageStateProvider<*>>,
    val render: @Composable (State) -> Unit,
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Page(
    val path: String,
    val state: KClass<out PageStateProvider<*>> = NoState::class,
)