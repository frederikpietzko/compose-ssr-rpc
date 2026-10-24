package dev.frederikpietzko.example.rpc.plumbing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.rpc.annotations.Rpc
import kotlinx.rpc.krpc.KrpcTransportMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.EOFException
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.util.concurrent.ConcurrentHashMap


@Component
@OptIn(ExperimentalStdlibApi::class)
class RpcWebsocketHandler<@Rpc Service : Any>(
    private val rpcServiceDefinitions: RpcBeanDefinitions<Service>,
) : TextWebSocketHandler() {
    companion object {
        private val log = LoggerFactory.getLogger(RpcWebsocketHandler::class.java)
    }

    private val sessions = ConcurrentHashMap<String, RpcServer>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        coroutineScope.launch {
            val krpcServer = getKrpcServer(session)
            krpcServer.pipeMessage(message.toKrpcMessage())
        }
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        coroutineScope.launch {
            val krpcServer = getKrpcServer(session)
            krpcServer.pipeMessage(message.toKrpcMessage())
        }
    }


    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        if (exception.isClientDisconnect()) {
            log.debug("WebSocket {} disconnected abruptly: {}", session.id, exception.message)
        } else {
            log.warn("Transport error on WebSocket {}", session.id, exception)
        }
        disposeSession(session.id)
        if (session.isOpen) {
            runCatching { session.close(CloseStatus.SERVER_ERROR) }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        disposeSession(session.id)
    }

    private fun disposeSession(sessionId: String) {
        sessions.remove(sessionId)?.close()
    }

    private fun Throwable.isClientDisconnect(): Boolean =
        this is EOFException ||
                this is ClosedChannelException ||
                (this is IOException && message?.contains("Broken pipe", ignoreCase = true) == true)

    private fun getKrpcServer(session: WebSocketSession): RpcServer {
        return sessions.getOrPutIfMissing(session.id) { createKrpcServer(session) }
    }

    private fun createKrpcServer(session: WebSocketSession): RpcServer {
        val server = RpcServer(WebSocketTransport(session))
        rpcServiceDefinitions.rpcServices.forEach { (clazz, instance) ->
            server.registerService(clazz) { instance }
        }
        return server
    }

    private fun TextMessage.toKrpcMessage() = KrpcTransportMessage.StringMessage(payload)
    private fun BinaryMessage.toKrpcMessage() = KrpcTransportMessage.BinaryMessage(payload.array())
}

