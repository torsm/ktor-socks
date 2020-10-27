package de.torsm.socks

internal enum class SOCKSCommand(val code: Byte) {
    CONNECT(1),
    BIND(2),
    UDP_ASSOCIATE(3);

    companion object {
        fun byCode(code: Byte): SOCKSCommand = values().find { it.code == code }
            ?: throw SOCKSException("Invalid SOCKS command: $code")
    }
}
