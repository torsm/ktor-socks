package de.torsm.socks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException


public fun CoroutineScope.socksServer(config: SOCKSConfig): SOCKSServer
        = SOCKSServer(config, coroutineContext)

public fun CoroutineScope.socksServer(block: SOCKSConfigBuilder.() -> Unit = {}): SOCKSServer
        = socksServer(SOCKSConfigBuilder().apply(block).build())

public fun socksServer(config: SOCKSConfig): SOCKSServer
        = SOCKSServer(config, Dispatchers.IO)

public fun socksServer(block: SOCKSConfigBuilder.() -> Unit = {}): SOCKSServer
        = socksServer(SOCKSConfigBuilder().apply(block).build())


public class SOCKSException(message: String, cause: Throwable? = null) : IOException(message, cause)
