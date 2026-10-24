package dev.frederikpietzko.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.rpc.withService
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.HydrationRoot
import org.jetbrains.compose.web.attributes.ScriptType
import org.jetbrains.compose.web.attributes.src
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.rehydrateRoot

@Composable
fun App(initialMessage: String) {
    val messageService = rpcClient.withService<MessageService>()
    val scope = rememberCoroutineScope()
    Main {
        Button(attrs = {
            onClick {
                scope.launch {
                    rehydrateRoot(
                        html = messageService.getMessage(),
                        hydrationId = "app",
                        deserializeState = { Json.decodeFromString<State>(it) },
                    ) { state ->
                        App(state.initialMessage)
                    }
                }
            }
        }) {
            Text(initialMessage)
        }
    }
}

@Composable
fun Site() {
    Html(attrs = { lang("en") }) {
        Head {
            Meta { attr("charset", "UTF-8") }
            Meta {
                attr("name", "viewport")
                attr("content", "width=device-width, initial-scale=1")
            }
            Title { Text("Compose SSR with Hydration and kotlinx.rpc") }
        }
        Body {
            HydrationRoot(State("Press Me!"), Json::encodeToString, hydrationId = "app") { initialState ->
                App(initialState.initialMessage)
            }
        }
        Script(attrs = {
            type(ScriptType.Module)
            src("/search-client.mjs")
        })
    }
}
