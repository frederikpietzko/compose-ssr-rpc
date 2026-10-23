package dev.frederikpietzko.example

import dev.frederikpietzko.example.plumbing.KrpcService
import org.jetbrains.compose.web.renderHydratedDocument

@KrpcService
class MessageServiceImpl : MessageService {
    override suspend fun getMessage(): String {
        return renderHydratedDocument {
            App("Hello World!")
        }
    }
}