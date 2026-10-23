package dev.frederikpietzko.example

import kotlinx.rpc.annotations.Rpc

@Rpc
interface MessageService {
    suspend fun getMessage(): String
}