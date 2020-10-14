package de.torsm.socks

public enum class SOCKSAddressType(public val code: Byte) {
    IPV4(1),
    IPV6(4),
    HOSTNAME(3);

    public companion object {
        public fun byCode(code: Byte): SOCKSAddressType = values().find { it.code == code }
            ?: throw SOCKSException("Invalid SOCKS address type: $code")
    }
}
