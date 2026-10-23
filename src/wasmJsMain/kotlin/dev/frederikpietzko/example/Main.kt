@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.frederikpietzko.example

import androidx.compose.runtime.Composition
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.hydrateRoot
import org.jetbrains.compose.web.rehydrateRoot

@JsFun("message => console.error(message)")
external fun consoleError(message: String)

private var currentComposition: Composition? = null

fun main() {
    currentComposition = hydrateRoot(
        hydrationId = "app",
        deserializeState = { Json.decodeFromString<State>(it) },
        onHydrationMismatch = { mismatch ->
            consoleError("Compose hydration failed; falling back to client rendering.\n${mismatch.stackTraceToString()}")
        }
    ) { state ->
        App(state.initialMessage, ::replaceApp)
    }
}

private fun replaceApp(html: String) {
    currentComposition = rehydrateRoot(
        html = html,
        previousComposition = currentComposition,
        hydrationId = "app",
        deserializeState = { Json.decodeFromString<State>(it) },
        onHydrationMismatch = { mismatch ->
            consoleError("Compose hydration failed; falling back to client rendering.\n${mismatch.stackTraceToString()}")
        },
    ) { state ->
        App(state.initialMessage, ::replaceApp)
    }
}
