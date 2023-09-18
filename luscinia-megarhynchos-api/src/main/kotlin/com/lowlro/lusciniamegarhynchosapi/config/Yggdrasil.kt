package com.lowlro.lusciniamegarhynchosapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "yggdrasil")
data class Yggdrasil(
    val token: Token = Token(),
    val core: Core = Core(),
    val rateLimit: RateLimit = RateLimit(),
    val session: Session = Session(),
    val signatureKey: SignatureKey = SignatureKey()
) {
    data class Token(
        val timeToFullyExpired: Long = 20,
        val enableTimeToPartiallyExpired: Boolean = true,
        val timeToPartiallyExpired: Long = 10,
        val onlyLastSessionAvailable: Boolean = false
    )

    data class Core(
        val serverName: String = "luscinia-megarhynchos",
        val url: String = "http://localhost:8080/yggdrasil/",
        val skinDomains: Array<String> = arrayOf("localhost"),
        val loginWithCharacterName: Boolean = true
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Core
            return skinDomains.contentEquals(other.skinDomains)
        }

        override fun hashCode(): Int {
            return skinDomains.contentHashCode()
        }
    }

    data class RateLimit(
        val limitDuration: Long = 300
    )

    data class Session(
        val authExpireTime: Long = 15
    )

    data class SignatureKey(
        val public: String = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAtAVmadHCXWffFkNlIErSlA1UKoB6PWlCjQXKYX2ckYOxB59taLstSNHTsG7op3QIFJXB7LmwG+ZXOp+uc7GZc4hb9mrQhf5Alr3v5YxsOL5+6bAZeo4LqKWmXfIaIhd4hk77ZO3g+k39Zx1gPLZMKkq6QFXMBBcju1hu/4wnZ9VL9LPftYem20QwB4n6rQAFISuMrGfqrVsYj9JXZ81lnESCcim16h6R3B/leHkHa5FTTS+z9ua/xBIlLDmr6Z4256qtDZ5b1q9svkqYGQL7z9I+a+DeWIYEaSCvwyfzncv5bUV+whH36kHOqi8Z0epEsGl8X4j3T1vSZhzN3HFU0iAs9JubzvEFOSbz6NhFk+IygEPngT4gMi8jh8Fgqvki0rOZ/fCN91O0/cx+PqxrgFw7we35is5OZyyDZzfK3iVqFvqOqpHa8svStbEzx96vhuQyiezIO+du+9/Cznl1DydZzwdIbKbiGTCBs03vSh/w1Cl9lPocsnm62vjLKCPSdo1GhO0ASmC339nyrPjs+HT1XskPoqUJFw7EmWQ+VoRRxeKz0DWEZqmOrX4bivM/Khfqw7+uRxvdWX0tEhyHaQw0isxA2dRU90g7DJYXbMLCH61EIg9hXh93XqV3Jph2T2JfpYRy0khQpDV5DHLRUDfncolmDX1VPtP1li1U2XMCAwEAAQ==",
        val private: String = "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"
    )
}