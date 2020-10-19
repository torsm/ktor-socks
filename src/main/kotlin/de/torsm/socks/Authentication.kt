package de.torsm.socks

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*


/**
 * Represents a SOCKS5 authentication method that the server and client can agree to use
 */
public interface SOCKSAuthenticationMethod {
    /**
     * Number used to identify the authentication method used with the SOCKS protocol
     */
    public val code: Byte

    /**
     * After this authentication method was chosen by the server and client, this method is called and should handle
     * communication to the client using [reader] and [writer] until the authentication has succeeded or failed.
     *
     * After all communication is complete and the client successfully authenticated itself, the method returns.
     *
     * If the client can't authenticate successfully using this method, and the connection should therefore be closed,
     * this method must throw a [SOCKSException] with an appropriate error message.
     */
    public suspend fun negotiate(reader: ByteReadChannel, writer: ByteWriteChannel)
}


/**
 * In case of an "open" SOCKS server where no authentication is needed, this method is chosen which adds no further
 * negotiation.
 */
public object NoAuthentication : SOCKSAuthenticationMethod {
    override val code: Byte = 0

    override suspend fun negotiate(reader: ByteReadChannel, writer: ByteWriteChannel) {}
}


/**
 * Username/Password authentication as specified in RFC 1929.
 *
 * This abstract class handles client communication, subclasses are responsible to actually verify the username/password
 * combination of the client by implementing the [verify] method.
 *
 * For example, a subclass can be created which verifies the username/password combination against a database of users.
 */
public abstract class UsernamePasswordAuthentication : SOCKSAuthenticationMethod {
    override val code: Byte = 2

    /**
     * Implementations of this method should return `true` if the username/password combination is valid, and `false`
     * otherwise.
     */
    public abstract fun verify(username: String, password: String): Boolean

    override suspend fun negotiate(reader: ByteReadChannel, writer: ByteWriteChannel) {
        val version = reader.readByte()
        if (version != VERSION) {
            throw SOCKSException("Invalid Username/Password authentication version: $version")
        }

        val usernameSize = reader.readByte().toInt()
        val username = reader.readPacket(usernameSize).readBytes().decodeToString()
        val passwordSize = reader.readByte().toInt()
        val password = reader.readPacket(passwordSize).readBytes().decodeToString()

        if (verify(username, password)) {
            writer.writeResponse(SUCCESS)
        } else {
            writer.writeResponse(FAILURE)
            throw SOCKSException("Username/Password authentication failed")
        }
    }

    private suspend fun ByteWriteChannel.writeResponse(status: Byte) {
        writePacket {
            writeByte(VERSION)
            writeByte(status)
        }
        flush()
    }

    private companion object {
        private const val VERSION = 1.toByte()
        private const val SUCCESS = 0.toByte()
        private const val FAILURE = 1.toByte()
    }
}
