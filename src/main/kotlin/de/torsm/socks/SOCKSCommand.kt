package de.torsm.socks

public enum class SOCKSCommand(public val code: Byte) {
    CONNECT(1),
    BIND(2),
    UDP_ASSOCIATE(3);

    public companion object {
        public fun byCode(code: Byte): SOCKSCommand = values().find { it.code == code }
            ?: throw SOCKSException("Invalid SOCKS command: $code")
    }
}
