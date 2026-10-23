package dev.frederikpietzko.example

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.rpc.withService
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.Text
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