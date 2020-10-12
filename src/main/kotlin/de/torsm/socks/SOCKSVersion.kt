package de.torsm.socks

enum class SOCKSVersion(val code: Byte, val replyVersion: Byte, val successCode: Byte, val unreachableHostCode: Byte) {
    SOCKS4(4, replyVersion = 0, successCode = 90, unreachableHostCode = 91),
    SOCKS5(5, replyVersion = 5, successCode = 0, unreachableHostCode = 4);

    companion object {
        fun byCode(code: Byte) = values().find { it.code == code }
            ?: throw SOCKSException("Invalid SOCKS version: $code")
    }
}
