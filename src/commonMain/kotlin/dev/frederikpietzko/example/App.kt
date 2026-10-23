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
    var message by remember { mutableStateOf(initialMessage) }
    val scope = rememberCoroutineScope()
    Main {
        Button(attrs = {
            onClick {
                scope.launch {
                    message = messageService.getMessage()
                    rehydrateRoot(
                        hydrationId = "app",
                        html = message,
                        deserializeState = { Json.decodeFromString<State>(it) },
                    ) {
                        App(it.initialMessage)
                    }
                }
            }
        }) {
            Text(message)
        }
    }
}