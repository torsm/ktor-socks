package de.torsm.socks

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.flow
import java.net.Inet4Address
import java.net.InetAddress
import java.nio.ByteBuffer


internal val InetAddress.isSOCKS4a: Boolean
    get() = when (this) {
        is Inet4Address -> {
            val address = address
            address[0] == 0.toByte()
                    && address[1] == 0.toByte()
                    && address[2] == 0.toByte()
                    && address[3] != 0.toByte()
        }
        else -> false
    }

private val terminatorByte = ByteBuffer.wrap(byteArrayOf(0))


internal suspend fun ByteReadChannel.readNullTerminatedString(bufferSize: Int = 1024): String {
    val buffer = ByteBuffer.allocate(bufferSize)
    val builder = StringBuilder()

    while (true) {
        val bytesRead = readUntilDelimiter(terminatorByte, buffer)
        if (bytesRead == 0) break

        val array = ByteArray(bytesRead)
        buffer.position(0)
        buffer.get(array)
        buffer.clear()

        builder.append(String(array))
    }
    skipDelimiter(terminatorByte)
    return builder.toString()
}


internal inline fun <C : ReadWriteSocket, R> C.useWithChannels(block: (C, ByteReadChannel, ByteWriteChannel) -> R): R {
    val reader = openReadChannel()
    val writer = openWriteChannel()
    var cause: Throwable? = null
    return try {
        block(this, reader, writer)
    } catch (e: Throwable) {
        cause = e
        throw e
    } finally {
        reader.cancel(cause)
        writer.close(cause)
        close()
    }
}
