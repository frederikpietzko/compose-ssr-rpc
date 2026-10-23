package dev.frederikpietzko.example

import dev.frederikpietzko.example.plumbing.KrpcService

@KrpcService
class MessageServiceImpl : MessageService {
    override suspend fun getMessage(): String {
        return "Hello World!"
    }
}