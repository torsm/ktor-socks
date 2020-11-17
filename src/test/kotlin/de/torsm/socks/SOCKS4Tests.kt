package de.torsm.socks

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import org.junit.jupiter.api.extension.ExtendWith
import java.net.Socket
import kotlin.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockServers::class)
@AllowSOCKS4
class SOCKS4Tests {

    @Test
    fun `Ping Pong`() {
        createClientSocket(4).use { clientSocket ->
            clientSocket.connect(mockServer)
            clientSocket.ping()
            clientSocket.assertPong()
        }
    }

    @Test
    fun `4a Ping Pong`() {
        // Java socket api does not support SOCKS4a
        Socket().use { clientSocket ->
            clientSocket.connect(proxyServer)
            val output = clientSocket.getOutputStream().asOutput()
            val input = clientSocket.getInputStream()
            output.run {
                writeByte(4)                    // protocol
                writeByte(1)                    // connect
                writeShort(8080)                // port
                writeInt(1)                     // ip - 0.0.0.1
                writeByte(0)                    // userid
                writeText("localhost")          // hostname
                writeByte(0)                    //
                flush()
            }
            assertEquals(input.read(), 0)       // protocol
            assertEquals(input.read(), 90)      // success
            input.readNBytes(6)                 // port + ip

            clientSocket.ping()
            clientSocket.assertPong()
        }
    }
}
