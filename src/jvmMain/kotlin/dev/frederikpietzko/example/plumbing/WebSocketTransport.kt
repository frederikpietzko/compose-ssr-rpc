package dev.frederikpietzko.example.plumbing

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import kotlin.coroutines.CoroutineContext

class WebSocketTransport(
    private val session: WebSocketSession,
    val outChannel: Channel<KrpcTransportMessage> = Channel(),
) : KrpcTransport {
    override val coroutineContext: CoroutineContext = Job()
    private val mutex = Mutex()

    override suspend fun send(message: KrpcTransportMessage) {
        mutex.withLock {
            when (message) {
                is KrpcTransportMessage.StringMessage -> {
                    session.sendMessage(TextMessage(message.value))
                }

                is KrpcTransportMessage.BinaryMessage -> {
                    session.sendMessage(BinaryMessage(message.value))
                }
            }
        }
    }

    override suspend fun receive(): KrpcTransportMessage {
        return outChannel.receive()
    }

    fun dispose() {
        outChannel.close()
        (coroutineContext[Job] as CompletableJob).cancel()
    }
}