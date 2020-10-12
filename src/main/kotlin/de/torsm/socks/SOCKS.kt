package de.torsm.socks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException


fun CoroutineScope.socksServer(config: SOCKSConfig): SOCKSServer
        = SOCKSServer(config, coroutineContext)

fun CoroutineScope.socksServer(block: SOCKSConfigBuilder.() -> Unit = {})
        = socksServer(SOCKSConfigBuilder().apply(block).build())

fun socksServer(config: SOCKSConfig): SOCKSServer
        = SOCKSServer(config, Dispatchers.IO)

fun socksServer(block: SOCKSConfigBuilder.() -> Unit = {})
        = socksServer(SOCKSConfigBuilder().apply(block).build())


class SOCKSException(message: String, cause: Throwable? = null) : IOException(message, cause)
