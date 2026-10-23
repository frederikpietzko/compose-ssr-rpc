package dev.frederikpietzko.example

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.rpc.withService
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.Text

@Composable
fun App(initialMessage: String, onReplace: (String) -> Unit = {}) {
    val messageService = rpcClient.withService<MessageService>()
    val scope = rememberCoroutineScope()
    Main {
        Button(attrs = {
            onClick {
                scope.launch {
                    onReplace(messageService.getMessage())
                }
            }
        }) {
            Text(initialMessage)
        }
    }
}