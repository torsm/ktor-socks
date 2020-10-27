package de.torsm.socks

internal enum class SOCKSVersion(
    val code: Byte,
    val replyVersion: Byte,
    val successCode: Byte,
    val unreachableHostCode: Byte,
    val connectionRefusedCode: Byte
) {
    SOCKS4(4, replyVersion = 0, successCode = 90, unreachableHostCode = 91, connectionRefusedCode = 91),
    SOCKS5(5, replyVersion = 5, successCode = 0, unreachableHostCode = 4, connectionRefusedCode = 5);

    companion object {
        fun byCode(code: Byte): SOCKSVersion = values().find { it.code == code }
            ?: throw SOCKSException("Invalid SOCKS version: $code")
    }
}
