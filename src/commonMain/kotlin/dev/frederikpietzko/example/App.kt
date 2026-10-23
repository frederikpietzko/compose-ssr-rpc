package dev.frederikpietzko.example

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.rpc.withService
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.Text

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
                }
            }
        }) {
            Text(message)
        }
    }
}