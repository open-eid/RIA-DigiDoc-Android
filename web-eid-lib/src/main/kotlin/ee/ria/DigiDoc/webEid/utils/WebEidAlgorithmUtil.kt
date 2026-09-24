// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import org.json.JSONArray
import org.json.JSONObject
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64

object WebEidAlgorithmUtil {
    private val SUPPORTED_HASH_FUNCTIONS =
        listOf(
            "SHA-224",
            "SHA-256",
            "SHA-384",
            "SHA-512",
            "SHA3-224",
            "SHA3-256",
            "SHA3-384",
            "SHA3-512",
        )

    fun buildSupportedSignatureAlgorithms(publicKey: PublicKey): JSONArray =
        JSONArray().apply {
            when (publicKey) {
                is ECPublicKey -> {
                    SUPPORTED_HASH_FUNCTIONS.forEach { hashFunction ->
                        put(
                            JSONObject().apply {
                                put("cryptoAlgorithm", "ECC")
                                put("hashFunction", hashFunction)
                                put("paddingScheme", "NONE")
                            },
                        )
                    }
                }

                else -> throw IllegalArgumentException("Unsupported key type")
            }
        }

    fun getAlgorithm(publicKey: PublicKey): String =
        when (getEcKeySize(publicKey)) {
            256 -> "ES256"
            384 -> "ES384"
            521 -> "ES512"
            else -> throw IllegalArgumentException("Unsupported EC key length")
        }

    fun buildSignatureAlgorithm(
        publicKey: PublicKey,
        hashFunction: String,
    ): JSONObject =
        when (publicKey) {
            is ECPublicKey ->
                JSONObject().apply {
                    put("cryptoAlgorithm", "ECC")
                    put("hashFunction", hashFunction)
                    put("paddingScheme", "NONE")
                }

            is RSAPublicKey ->
                JSONObject().apply {
                    put("cryptoAlgorithm", "RSA")
                    put("hashFunction", hashFunction)
                    put("paddingScheme", "PKCS1.5")
                }

            else ->
                throw IllegalArgumentException(
                    "Unsupported key type: ${publicKey.algorithm}",
                )
        }

    fun parseCertificate(signingCertBase64: String): X509Certificate {
        val certBytes = Base64.getDecoder().decode(signingCertBase64)
        return CertificateFactory
            .getInstance("X.509")
            .generateCertificate(certBytes.inputStream()) as X509Certificate
    }

    private fun getEcKeySize(publicKey: PublicKey): Int =
        when (publicKey) {
            is ECPublicKey -> publicKey.params.curve.field.fieldSize
            else -> throw IllegalArgumentException("Unsupported key type")
        }
}
