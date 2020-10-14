package de.torsm.socks

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.*
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.core.use
import io.ktor.utils.io.joinTo
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

public class SOCKSServer internal constructor(private val config: SOCKSConfig, context: CoroutineContext): CoroutineScope {
    private val log = LoggerFactory.getLogger(javaClass)
    private val selector = ActorSelectorManager(Dispatchers.IO)

    override val coroutineContext: CoroutineContext
            = context + SupervisorJob(context[Job]) + CoroutineName("socks-server")

    public fun start() {
        val serverSocket = aSocket(selector).tcp().bind(config.networkAddress)
        log.info("Starting SOCKS proxy server on {}", serverSocket.localAddress)

        launch {
            serverSocket.use {
                while (true) {
                    val clientSocket = serverSocket.accept()
                    val clientName = clientSocket.remoteAddress.toString()
                    log.debug("Client connected: {}", clientName)

                    launchClientJob(clientSocket).invokeOnCompletion {
                        log.debug("Client disconnected: {}", clientName)
                    }
                }
            }
        }
    }

    private fun launchClientJob(clientSocket: Socket) = launch {
        clientSocket.useWithChannels { _, reader, writer ->
            val handshake = SOCKSHandshake(reader, writer, config, selector)
            handshake.negotiate()
            handshake.hostSocket.useWithChannels { _, hostReader, hostWriter ->
                coroutineScope {
                    relayApplicationData(reader, hostWriter)
                    relayApplicationData(hostReader, writer)
                }
            }
        }
    }

    private fun CoroutineScope.relayApplicationData(src: ByteReadChannel, dst: ByteWriteChannel) {
        launch {
            try {
                src.joinTo(dst, false)
            } catch (ignored: Throwable) {
                /*
                * Exceptions while relaying channel traffic (due to closed sockets for example)
                * are not exceptional and are considered the natural end of client/host communication
                */
            }
        }
    }
}
