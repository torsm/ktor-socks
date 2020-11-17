package de.torsm.socks

import org.junit.jupiter.api.extension.ExtendWith
import java.net.SocketException
import kotlin.test.Test
import kotlin.test.assertFailsWith


@ExtendWith(MockServers::class)
@ServerCredentials("username", "correct password")
class AuthenticationTests {

    @Test
    @ClientCredentials("username", "correct password")
    fun `SOCKS5 Authenticated Ping Pong`() {
        createClientSocket(5).use { clientSocket ->
            clientSocket.connect(mockServer)
            clientSocket.ping()
            clientSocket.assertPong()
        }
    }

    @Test
    @ClientCredentials("username", "wrong password")
    fun `SOCKS5 Authentication failed`() {
        createClientSocket(5).use { clientSocket ->
            assertFailsWith<SocketException> {
                clientSocket.connect(mockServer)
            }
        }
    }
}
