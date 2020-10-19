package de.torsm.socks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * Creates a [SOCKSServer] with the given [configuration][config], using the receiving [CoroutineScope]'s
 * [CoroutineContext]
 */
public fun CoroutineScope.socksServer(config: SOCKSConfig): SOCKSServer
        = SOCKSServer(config, coroutineContext)

/**
 * Creates a [SOCKSServer] with the given configuration [block], using the receiving [CoroutineScope]'s
 * [CoroutineContext]
 */
public fun CoroutineScope.socksServer(block: SOCKSConfigBuilder.() -> Unit = {}): SOCKSServer
        = SOCKSServer(SOCKSConfigBuilder().apply(block).build(), coroutineContext)

/**
 * Creates a [SOCKSServer] with the given [configuration][config] and [context]
 */
public fun socksServer(context: CoroutineContext = Dispatchers.IO, config: SOCKSConfig): SOCKSServer
        = SOCKSServer(config, context)

/**
 * Creates a [SOCKSServer] with the given configuration [block] and [context]
 */
public fun socksServer(context: CoroutineContext = Dispatchers.IO, block: SOCKSConfigBuilder.() -> Unit = {}): SOCKSServer
        = SOCKSServer(SOCKSConfigBuilder().apply(block).build(), context)


/**
 * Represents any kind of protocol-level error
 */
public class SOCKSException(message: String, cause: Throwable? = null) : IOException(message, cause)
