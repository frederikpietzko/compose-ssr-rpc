package dev.frederikpietzko.example

import io.ktor.client.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json


val ktorClient = HttpClient {
    installKrpc {
        serialization {
            json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                prettyPrint = true
            }
        }
    }
}

val rpcClient = ktorClient.rpc("ws://localhost:4200/services") {
    rpcConfig {
        serialization {
            json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                prettyPrint = true
            }
        }
    }
}
