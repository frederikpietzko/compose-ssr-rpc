package dev.frederikpietzko.example.rpc.plumbing

import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.krpc.server.KrpcServer

class RpcServer(private val transport: WebSocketTransport) : KrpcServer(rpcServerConfig {
    serialization {
        json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}, transport = transport) {


    suspend fun pipeMessage(message: KrpcTransportMessage) {
        transport.outChannel.send(message)
    }

    fun close() {
        super.close(null)
        transport.dispose()
    }
}