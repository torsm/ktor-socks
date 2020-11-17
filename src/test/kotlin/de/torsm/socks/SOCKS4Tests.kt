package de.torsm.socks

import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

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
}
