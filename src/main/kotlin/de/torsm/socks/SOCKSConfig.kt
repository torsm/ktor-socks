package de.torsm.socks

import io.ktor.util.network.*

public class SOCKSConfig(
    public val allowSOCKS4: Boolean,
    public val authenticationMethods: List<SOCKSAuthenticationMethod>,
    public val networkAddress: NetworkAddress
)

public class SOCKSConfigBuilder {
    public val authenticationMethods: MutableList<SOCKSAuthenticationMethod> = mutableListOf()

    public var allowSOCKS4: Boolean = true

    public var networkAddress: NetworkAddress? = null

    public var hostname: String = "0.0.0.0"

    public var port: Int = 1080

    public fun build(): SOCKSConfig = SOCKSConfig(
        allowSOCKS4,
        authenticationMethods.ifEmpty { mutableListOf(NoAuthentication) },
        networkAddress ?: NetworkAddress(hostname, port)
    )
}

public fun SOCKSConfigBuilder.addAuthenticationMethod(method: SOCKSAuthenticationMethod) {
    authenticationMethods.add(method)
}
