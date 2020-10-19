package de.torsm.socks

internal enum class SOCKSAddressType(val code: Byte) {
    IPV4(1),
    IPV6(4),
    HOSTNAME(3);

    companion object {
        fun byCode(code: Byte): SOCKSAddressType = values().find { it.code == code }
            ?: throw SOCKSException("Invalid SOCKS address type: $code")
    }
}
