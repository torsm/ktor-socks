package de.torsm.socks

enum class SOCKSAddressType(val code: Byte) {
    IPV4(1),
    IPV6(4),
    HOSTNAME(3);

    companion object {
        fun byCode(code: Byte) = values().find { it.code == code }
            ?: throw SOCKSException("Invalid SOCKS address type: $code")
    }
}
