@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import org.json.JSONArray
import org.json.JSONObject
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebEidAuthServiceImpl
    @Inject
    constructor() : WebEidAuthService {

        companion object {
            val SUPPORTED_HASH_FUNCTIONS = listOf(
                "SHA-224", "SHA-256", "SHA-384", "SHA-512",
                "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512"
            )
        }

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
            val algorithm = getAlgorithm(publicKey)

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

        private fun getAlgorithm(publicKey: PublicKey): String =
            when (publicKey) {
                is ECPublicKey -> {
                    when (publicKey.params.curve.field.fieldSize) {
                        256 -> "ES256"
                        384 -> "ES384"
                        521 -> "ES512"
                        else -> throw IllegalArgumentException("Unsupported EC key length")
                    }
                }

                else -> throw IllegalArgumentException("Unsupported key type")
            }

    private fun buildSupportedSignatureAlgorithms(publicKey: PublicKey): JSONArray =
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
    }
