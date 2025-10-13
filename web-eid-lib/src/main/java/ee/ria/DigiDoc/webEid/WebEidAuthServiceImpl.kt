@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import org.json.JSONArray
import org.json.JSONObject
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebEidAuthServiceImpl
    @Inject
    constructor() : WebEidAuthService {
        override fun buildAuthToken(
            authCert: ByteArray,
            signingCert: ByteArray?,
            signature: ByteArray,
        ): JSONObject {
            val cert =
                CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(authCert.inputStream()) as X509Certificate

            val publicKey = cert.publicKey
            val algorithm =
                when (publicKey) {
                    is RSAPublicKey -> "RS256"
                    is ECPublicKey -> "ES384"
                    else -> "RS256"
                }

            return JSONObject().apply {
                put("algorithm", algorithm)
                put("unverifiedCertificate", Base64.getEncoder().encodeToString(authCert))
                put("issuerApp", "https://web-eid.eu/web-eid-mobile-app/releases/v1.0.0")
                put("signature", Base64.getEncoder().encodeToString(signature))

                if (signingCert != null) {
                    val supportedSignatureAlgorithms = buildSupportedSignatureAlgorithms(publicKey)
                    put("unverifiedSigningCertificate", Base64.getEncoder().encodeToString(signingCert))
                    put("supportedSignatureAlgorithms", supportedSignatureAlgorithms)
                    put("format", "web-eid:1.1")
                } else {
                    put("format", "web-eid:1.0")
                }
            }
        }

        private fun buildSupportedSignatureAlgorithms(publicKey: PublicKey): JSONArray =
            JSONArray().apply {
                when (publicKey) {
                    is RSAPublicKey -> {
                        val hashFunction =
                            when (publicKey.modulus.bitLength()) {
                                2048 -> "SHA-256"
                                3072 -> "SHA-384"
                                4096 -> "SHA-512"
                                else -> throw IllegalArgumentException("Unsupported RSA key length")
                            }
                        put(
                            JSONObject().apply {
                                put("cryptoAlgorithm", "RSA")
                                put("hashFunction", hashFunction)
                                put("paddingScheme", "PKCS1.5")
                            },
                        )
                    }
                    is ECPublicKey -> {
                        val hashFunction =
                            when (publicKey.params.curve.field.fieldSize) {
                                256 -> "SHA-256"
                                384 -> "SHA-384"
                                512 -> "SHA-512"
                                else -> throw IllegalArgumentException("Unsupported EC key length")
                            }
                        put(
                            JSONObject().apply {
                                put("cryptoAlgorithm", "EC")
                                put("hashFunction", hashFunction)
                                put("paddingScheme", "NONE")
                            },
                        )
                    }
                    else -> throw IllegalArgumentException("Unsupported key type")
                }
            }
    }
