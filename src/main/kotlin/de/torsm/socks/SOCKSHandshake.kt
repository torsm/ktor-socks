package de.torsm.socks

import de.torsm.socks.SOCKSAddressType.*
import de.torsm.socks.SOCKSCommand.*
import de.torsm.socks.SOCKSVersion.SOCKS4
import de.torsm.socks.SOCKSVersion.SOCKS5
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress

@Suppress("BlockingMethodInNonBlockingContext")
internal class SOCKSHandshake(
    private val reader: ByteReadChannel,
    private val writer: ByteWriteChannel,
    private val config: SOCKSConfig,
    private val selector: SelectorManager
) {
    lateinit var selectedVersion: SOCKSVersion
    lateinit var hostSocket: Socket

    suspend fun negotiate() {
        selectedVersion = reader.readVersion()
        when (selectedVersion) {
            SOCKS4 -> {
                if (!config.allowSOCKS4) {
                    sendReply(SOCKS4_REJECTED)
                    throw SOCKSException("SOCKS4 connection not allowed")
                }
            }
            SOCKS5 -> {
                handleAuthentication()
                // read version of SOCKS request
                check(reader.readVersion() == selectedVersion) { "Inconsistent SOCKS versions" }
            }
        }

        val request = receiveRequest()

        when (request.command) {
            CONNECT -> connect(request)
            BIND -> bind(request)
            UDP_ASSOCIATE -> {
                check(selectedVersion == SOCKS5) { "SOCKS4 does not support $UDP_ASSOCIATE" }
                sendReply(SOCKS5_UNSUPPORTED_COMMAND)
                throw SOCKSException("Unsupported command: $UDP_ASSOCIATE")
            }
        }
    }


    private suspend fun receiveRequest(): SOCKSRequest {
        val command: SOCKSCommand
        val address: InetAddress
        val port: Short

        when (selectedVersion) {
            SOCKS4 -> {
                command = reader.readCommand()
                port = reader.readShort()
                val ip = reader.readAddress()
                reader.readNullTerminatedString() // ignoring USERID field

                address = if (ip.isSOCKS4a) {
                    InetAddress.getByName(reader.readNullTerminatedString())
                } else {
                    ip
                }
            }
            SOCKS5 -> {
                command = reader.readCommand()
                reader.readByte() // reserved (RSV) field
                address = reader.readAddress()
                port = reader.readShort()
            }
        }

        return SOCKSRequest(command, address, port.toInt())
    }

    private suspend fun handleAuthentication() {
        val methodsCount = reader.readByte().toInt()
        val clientMethods = List(methodsCount) { reader.readByte() }
        val commonMethod = config.authenticationMethods.firstOrNull { it.code in clientMethods }

        if (commonMethod == null) {
            sendReply(SOCKS5_NO_ACCEPTABLE_METHODS)
            throw SOCKSException("No common authentication method found")
        } else {
            sendReply(commonMethod.code)
            commonMethod.negotiate(reader, writer)
        }
    }

    private suspend fun connect(request: SOCKSRequest) {
        val host = InetSocketAddress(request.destinationAddress, request.port)
        val socket = try {
            aSocket(selector).tcp().connect(host)
        } catch (e: Throwable) {
            sendReply(selectedVersion.unreachableHostCode)
            throw SOCKSException("Unreachable host: $host", e)
        }

        try {
            sendReply(selectedVersion.successCode, socket.localAddress as InetSocketAddress)
        } catch (e: Throwable) {
            socket.close()
            throw e
        }

        hostSocket = socket
    }

    private suspend fun bind(request: SOCKSRequest) {
        val socket = coroutineScope {
            val address = config.networkAddress.withPort(0)
            aSocket(selector).tcp().bind(address).use { serverSocket ->
                val socketJob = async {
                    serverSocket.accept()
                }

                sendReply(selectedVersion.successCode, serverSocket.localAddress as InetSocketAddress)

                socketJob.await()
            }
        }

        val hostAddress = socket.remoteAddress as InetSocketAddress

        if (hostAddress.address != request.destinationAddress) {
            sendReply(selectedVersion.connectionRefusedCode)
            socket.close()
            throw SOCKSException("Incoming host address (${hostAddress.address}) did not match requested host (${request.destinationAddress})")
        }

        try {
            sendReply(selectedVersion.successCode, hostAddress)
        } catch (e: Exception) {
            socket.close()
            throw e
        }

        hostSocket = socket
    }

    private suspend fun sendReply(code: Byte, writeAdditionalData: suspend BytePacketBuilder.() -> Unit = {}) {
        writer.writePacket {
            writeByte(selectedVersion.replyVersion)
            writeByte(code)
            writeAdditionalData()
        }
        writer.flush()
    }

    private suspend fun sendReply(code: Byte, address: InetSocketAddress) {
        sendReply(code) {
            if (selectedVersion == SOCKS5) writeByte(SOCKS5_RESERVED)
            writeAddress(address)
        }
    }



    private suspend fun ByteReadChannel.readVersion(): SOCKSVersion {
        val versionNumber = readByte()
        return SOCKSVersion.byCode(versionNumber)
    }

    private suspend fun ByteReadChannel.readCommand(): SOCKSCommand {
        val code = readByte()
        return SOCKSCommand.byCode(code)
    }

    private suspend fun ByteReadChannel.readAddress(): InetAddress {
        val addressType = when (selectedVersion) {
            SOCKS4 -> IPV4
            SOCKS5 -> SOCKSAddressType.byCode(readByte())
        }
        return when (addressType) {
            IPV4 -> {
                val data = readPacket(4)
                Inet4Address.getByAddress(data.readBytes())
            }
            IPV6 -> {
                val data = readPacket(16)
                Inet6Address.getByAddress(data.readBytes())
            }
            HOSTNAME -> {
                val size = readByte().toInt()
                val data = readPacket(size)
                InetAddress.getByName(data.readBytes().decodeToString())
            }
        }
    }

    private fun BytePacketBuilder.writeAddress(address: InetSocketAddress) {
        val port = address.port.toShort()
        val ip = address.address
        when (selectedVersion) {
            SOCKS4 -> {
                check(ip is Inet4Address || ip.isAnyLocalAddress) { "Expecting IPv4 address for SOCKS4" }
                writeShort(port)
                writeFully(ip.address, length = 4)
            }
            SOCKS5 -> {
                when (ip) {
                    is Inet4Address -> writeByte(IPV4.code)
                    is Inet6Address -> writeByte(IPV6.code)
                    else -> error("Unknown InetAddress type: ${ip.javaClass}")
                }
                writeFully(ip.address)
                writeShort(port)
            }
        }
    }

    private data class SOCKSRequest(
        val command: SOCKSCommand,
        val destinationAddress: InetAddress,
        val port: Int
    )
}

private const val SOCKS4_REJECTED = 91.toByte()
private const val SOCKS5_RESERVED = 0.toByte()
private const val SOCKS5_UNSUPPORTED_COMMAND = 7.toByte()
private const val SOCKS5_NO_ACCEPTABLE_METHODS = 0xFF.toByte()
