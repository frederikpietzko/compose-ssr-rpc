package dev.frederikpietzko.example

import dev.frederikpietzko.example.plumbing.KrpcService
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.HydrationRoot
import org.jetbrains.compose.web.renderHydratedFragment

@KrpcService
class MessageServiceImpl : MessageService {
    override suspend fun getMessage(): String {
        return renderHydratedFragment {
            HydrationRoot(State("Hello World!"), Json::encodeToString, hydrationId = "app") {
                App(it.initialMessage)
            }
        }
    }
}