package de.torsm.socks

import io.ktor.util.network.*
import java.net.InetSocketAddress

/**
 * [SOCKSServer] configuration
 *
 * @property allowSOCKS4 Whether the server should accept clients using SOCKS4, which doesn't support authentication
 * @property authenticationMethods List of supported [authentication methods][SOCKSAuthenticationMethod] for SOCKS5
 * @property networkAddress [NetworkAddress] the server should bind to
 */
public class SOCKSConfig(
    public val allowSOCKS4: Boolean,
    public val authenticationMethods: List<SOCKSAuthenticationMethod>,
    public val networkAddress: InetSocketAddress
)

/**
 * Builder class for [SOCKSConfig]
 *
 * By default, a config created by this builder will [allow][allowSOCKS4] SOCKS4 clients.
 *
 * By assigning a non-null value to [networkAddress], the created config will use that address.
 * Otherwise a combination of [hostname] and [port] is used (`0.0.0.0:1080` by default).
 *
 * If [authenticationMethods] remains empty, the created config will allow clients to use [NoAuthentication].
 */
public class SOCKSConfigBuilder {
    public val authenticationMethods: MutableList<SOCKSAuthenticationMethod> = mutableListOf()

    public var allowSOCKS4: Boolean = true

    public var networkAddress: InetSocketAddress? = null

    public var hostname: String = "0.0.0.0"

    public var port: Int = 1080

    public fun build(): SOCKSConfig = SOCKSConfig(
        allowSOCKS4,
        authenticationMethods.ifEmpty { mutableListOf(NoAuthentication) },
        networkAddress ?: InetSocketAddress(hostname, port)
    )
}

public fun SOCKSConfigBuilder.addAuthenticationMethod(method: SOCKSAuthenticationMethod) {
    authenticationMethods.add(method)
}
