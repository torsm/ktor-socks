package de.torsm.socks

import io.ktor.util.network.*

class SOCKSConfig(
    val allowSOCKS4: Boolean,
    val authenticationMethods: List<SOCKSAuthenticationMethod>,
    val networkAddress: NetworkAddress
)

class SOCKSConfigBuilder {
    val authenticationMethods: MutableList<SOCKSAuthenticationMethod> = mutableListOf()

    var allowSOCKS4: Boolean = true

    var networkAddress: NetworkAddress? = null

    var hostname: String = "0.0.0.0"

    var port: Int = 1080

    fun build() = SOCKSConfig(
        allowSOCKS4,
        authenticationMethods.ifEmpty { mutableListOf(NoAuthentication) },
        networkAddress ?: NetworkAddress(hostname, port)
    )
}

fun SOCKSConfigBuilder.addAuthenticationMethod(method: SOCKSAuthenticationMethod) {
    authenticationMethods.add(method)
}
