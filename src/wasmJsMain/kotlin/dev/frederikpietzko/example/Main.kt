@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.frederikpietzko.example

import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.hydrateRoot

@JsFun("message => console.error(message)")
private external fun consoleError(message: String)

fun main() {
    hydrateRoot(
        deserializeState = { Json.decodeFromString<State>(it) },
        onHydrationMismatch = { mismatch ->
            consoleError("Compose hydration failed; falling back to client rendering.\n${mismatch.stackTraceToString()}")
        }
    ) { state ->
        App(state.initialMessage)
    }
}
