@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

import android.net.Uri
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.webEid.utils.WebEidError
import ee.ria.DigiDoc.webEid.utils.WebEidErrorCodes
import ee.ria.DigiDoc.webEid.utils.WebEidResponseUtil
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebEidAuthParserImpl
    @Inject
    constructor() : WebEidAuthParser {
        private val logTag = javaClass.simpleName

        override fun handleAuthFlow(uri: Uri): String {
            return try {
                val request = parseAuthUri(uri)
                WebEidResponseUtil.createSuccessRedirect(request.loginUri)
            } catch (e: Exception) {
                errorLog(logTag, "Error in auth flow", e)
                val err = mapExceptionToError(e)
                WebEidResponseUtil.createErrorRedirect(extractLoginUriSafe(uri), err.code, err.message)
            }
        }

        override fun parseAuthUri(uri: Uri): WebEidAuthRequest {
            val json = decodeUriFragment(uri)

            val challenge = json.getString("challenge")
            val loginUriEncoded = json.getString("login_uri")
            val getSigningCertificate = json.optBoolean("get_signing_certificate", false)

            val loginUri = java.net.URLDecoder.decode(loginUriEncoded, java.nio.charset.StandardCharsets.UTF_8.name())

            validateHttpsScheme(loginUri)
            val origin = parseOriginFromLoginUri(loginUri)
            validateOriginCorrectness(loginUri, origin)

            return WebEidAuthRequest(
                challenge = challenge,
                loginUri = loginUri,
                getSigningCertificate = getSigningCertificate,
                origin = origin,
            )
        }

        override fun parseSignUri(uri: Uri): WebEidSignRequest {
            val json = decodeUriFragment(uri)
            return WebEidSignRequest(
                responseUri = json.getString("response_uri"),
                signCertificate = json.getString("sign_certificate"),
                hash = json.getString("hash"),
                hashFunction = json.getString("hash_function"),
            )
        }

        private fun decodeUriFragment(uri: Uri): JSONObject {
            try {
                val fragment = uri.fragment ?: throw IllegalArgumentException("No fragment in URI")
                val decoded = String(Base64.getDecoder().decode(fragment))
                return JSONObject(decoded)
            } catch (e: Exception) {
                errorLog(logTag, "Failed to decode or parse URI fragment: ${uri.fragment}", e)
                throw IllegalArgumentException("Invalid URI fragment format", e)
            }
        }

        private fun validateHttpsScheme(loginUri: String) {
            try {
                val parsed = URI(loginUri)
                if (!parsed.scheme.equals("https", ignoreCase = true)) {
                    errorLog(logTag, "Invalid scheme in login_uri: $loginUri — must be HTTPS")
                    throw IllegalArgumentException("login_uri must use HTTPS")
                }
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Exception) {
                errorLog(logTag, "Invalid login_uri format: $loginUri", e)
                throw IllegalArgumentException("Invalid login_uri format", e)
            }
        }

        private fun validateOriginCorrectness(
            loginUri: String,
            origin: String,
        ) {
            try {
                val parsedLogin = URI(loginUri)
                val expected = URI(origin)

                if (!parsedLogin.host.equals(expected.host, ignoreCase = true) ||
                    parsedLogin.port != expected.port
                ) {
                    errorLog(
                        logTag,
                        "Origin mismatch: expected $origin but login_uri points to host ${parsedLogin.host}",
                    )
                    throw IllegalArgumentException("Origin mismatch: expected $origin")
                }

                if (parsedLogin.userInfo != null) {
                    errorLog(
                        logTag,
                        "Login URI contains userinfo (possible phishing attempt): $loginUri",
                    )
                    throw IllegalArgumentException("Login URI contains userinfo (possible phishing attempt)")
                }
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Exception) {
                errorLog(logTag, "Failed to validate origin correctness for $loginUri", e)
                throw IllegalArgumentException("Invalid origin in login_uri", e)
            }
        }

        private fun parseOriginFromLoginUri(loginUri: String): String {
            return try {
                val parsed = URI(loginUri)
                if (parsed.scheme.isNullOrBlank() || parsed.host.isNullOrBlank()) {
                    errorLog(logTag, "Invalid login_uri: missing scheme or host — $loginUri")
                    return ""
                }
                val portPart = if (parsed.port != -1) ":${parsed.port}" else ""
                "${parsed.scheme}://${parsed.host}$portPart"
            } catch (e: Exception) {
                errorLog(logTag, "Failed to parse origin from login_uri: $loginUri", e)
                ""
            }
        }

        private fun extractLoginUriSafe(uri: Uri): String {
            return try {
                val json = decodeUriFragment(uri)
                json.optString("login_uri", "")
            } catch (e: Exception) {
                errorLog(logTag, "Failed to safely extract login_uri from URI: $uri", e)
                ""
            }
        }

        private fun mapExceptionToError(e: Exception): WebEidError {
            return when (e) {
                is IllegalArgumentException ->
                    WebEidError(
                        code = WebEidErrorCodes.INVALID_REQUEST,
                        message = e.message ?: WebEidErrorCodes.INVALID_REQUEST_MESSAGE,
                    )
                else ->
                    WebEidError(
                        code = WebEidErrorCodes.UNKNOWN,
                        message = WebEidErrorCodes.UNKNOWN_MESSAGE,
                    )
            }
        }

        override fun buildAuthToken(
            certBytes: ByteArray,
            signature: ByteArray,
            challenge: String,
        ): JSONObject {
            val cert =
                CertificateFactory.getInstance("X.509")
                    .generateCertificate(certBytes.inputStream()) as X509Certificate

            val publicKey = cert.publicKey
            val algorithm =
                when (publicKey) {
                    is java.security.interfaces.RSAPublicKey -> "RS256"
                    is java.security.interfaces.ECPublicKey -> "ES384"
                    else -> "RS256"
                }

            val supportedSignatureAlgorithms =
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put("cryptoAlgorithm", "RSA")
                            put("hashFunction", "SHA-256")
                            put("paddingScheme", "PKCS1.5")
                        },
                    )
                }

            return JSONObject().apply {
                put("algorithm", algorithm)
                put("unverifiedCertificate", Base64.getEncoder().encodeToString(certBytes))
                put("unverifiedSigningCertificate", Base64.getEncoder().encodeToString(certBytes))
                put("supportedSignatureAlgorithms", supportedSignatureAlgorithms)
                put("issuerApp", "https://web-eid.eu/web-eid-mobile-app/releases/v1.0.0")
                put("signature", Base64.getEncoder().encodeToString(signature))
                put("format", "web-eid:1.1")
                put("challenge", challenge)
            }
        }
    }
