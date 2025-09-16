@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.net.Uri
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import ee.ria.DigiDoc.webEid.utils.WebEidAuthParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private val logTag = javaClass.simpleName

        private val _authRequest = MutableStateFlow<WebEidAuthRequest?>(null)
        override val authRequest: StateFlow<WebEidAuthRequest?> = _authRequest.asStateFlow()

        private val _signRequest = MutableStateFlow<WebEidSignRequest?>(null)
        override val signRequest: StateFlow<WebEidSignRequest?> = _signRequest.asStateFlow()

        private val _errorState = MutableStateFlow<String?>(null)
        override val errorState: StateFlow<String?> = _errorState.asStateFlow()

        override fun parseAuthUri(uri: Uri) {
            try {
                _authRequest.value = WebEidAuthParser.parseAuthUri(uri)
            } catch (e: IllegalArgumentException) {
                errorLog(logTag, "Validation failed in parseAuthUri", e)
                _errorState.value = e.message
            } catch (e: Exception) {
                errorLog(logTag, "Failed to parse Web eID auth URI", e)
                _errorState.value = e.message
            }
        }

        override fun parseSignUri(uri: Uri) {
            try {
                _signRequest.value = WebEidAuthParser.parseSignUri(uri)
            } catch (e: IllegalArgumentException) {
                errorLog(logTag, "Validation failed in parseSignUri", e)
                _errorState.value = e.message
            } catch (e: Exception) {
                errorLog(logTag, "Failed to parse Web eID sign URI", e)
                _errorState.value = e.message
            }
        }

        override fun buildAuthToken(
            authCert: ByteArray,
            signingCert: ByteArray,
            signature: ByteArray,
            challenge: String,
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

            val getSigningCertificate = authRequest.value?.getSigningCertificate == true

            return JSONObject().apply {
                put("algorithm", algorithm)
                put("unverifiedCertificate", Base64.getEncoder().encodeToString(authCert))
                put("issuerApp", "https://web-eid.eu/web-eid-mobile-app/releases/v1.0.0")
                put("signature", Base64.getEncoder().encodeToString(signature))
                put("challenge", challenge)

                if (getSigningCertificate) {
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
