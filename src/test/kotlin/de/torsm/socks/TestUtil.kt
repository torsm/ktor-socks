@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package de.torsm.socks

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import kotlin.test.assertEquals


val proxyServer = InetSocketAddress(InetAddress.getLocalHost(), 1080)
val mockServer = InetSocketAddress(InetAddress.getLocalHost(), 8080)

fun createClientSocket(socksVersion: Int): Socket {
    System.setProperty("socksProxyVersion", socksVersion.toString())
    val proxy = Proxy(Proxy.Type.SOCKS, proxyServer)
    return Socket(proxy)
}

fun Socket.ping() {
    getOutputStream().bufferedWriter().run {
        write("ping\n")
        flush()
    }
}

fun Socket.assertPong() {
    getInputStream().bufferedReader().run {
        assertEquals("pong", readLine())
    }
}
