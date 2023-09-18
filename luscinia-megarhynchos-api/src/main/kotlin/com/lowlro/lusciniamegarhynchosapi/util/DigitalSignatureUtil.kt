package com.lowlro.lusciniamegarhynchosapi.util

import com.lowlro.lusciniamegarhynchosapi.config.Yggdrasil
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Component
class DigitalSignatureUtil(
    val config: Yggdrasil
) {
    fun sign(update: String): String {
        val signature = Signature.getInstance("SHA1withRSA")
        signature.initSign(
            KeyFactory.getInstance("RSA").generatePrivate(
                PKCS8EncodedKeySpec(
                    Base64.getDecoder().decode(config.signatureKey.private)
                )
            ), SecureRandom()
        )
        signature.update(update.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    fun verify(update: String, verify: String): Boolean {
        val signature = Signature.getInstance("SHA1withRSA")
        signature.initVerify(
            KeyFactory.getInstance("RSA").generatePublic(
                X509EncodedKeySpec(
                    Base64.getDecoder().decode(config.signatureKey.public)
                )
            )
        )
        signature.update(update.toByteArray(Charsets.UTF_8))
        return signature.verify(Base64.getDecoder().decode(verify))
    }
}