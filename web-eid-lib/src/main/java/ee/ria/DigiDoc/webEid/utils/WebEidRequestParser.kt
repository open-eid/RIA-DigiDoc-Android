/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.net.Uri
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidCertificateRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import ee.ria.DigiDoc.webEid.exception.WebEidErrorCode.ERR_WEBEID_MOBILE_INVALID_REQUEST
import ee.ria.DigiDoc.webEid.exception.WebEidException
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.util.Base64

object WebEidRequestParser {
    private const val MIN_CHALLENGE_LENGTH = 44
    private const val MAX_CHALLENGE_LENGTH = 128
    private const val MAX_ORIGIN_LENGTH = 255

    fun parseAuthUri(authUri: Uri): WebEidAuthRequest {
        val request = decodeUriFragment(authUri)
        val challenge = request.getString("challenge")
        val responseUri = validateResponseUri(request.getString("login_uri"))
        if (challenge.isNullOrBlank() ||
            challenge.length < MIN_CHALLENGE_LENGTH ||
            challenge.length > MAX_CHALLENGE_LENGTH
        ) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Invalid challenge length",
                responseUri.toString(),
            )
        }

        return WebEidAuthRequest(
            challenge = challenge,
            loginUri = responseUri.toString(),
            getSigningCertificate = request.optBoolean("get_signing_certificate", false),
            origin = parseOrigin(responseUri),
        )
    }

    fun parseCertificateUri(uri: Uri): WebEidCertificateRequest {
        val request = decodeUriFragment(uri)
        val responseUri = validateResponseUri(request.optString("response_uri", ""))

        return WebEidCertificateRequest(
            responseUri = responseUri.toString(),
            origin = parseOrigin(responseUri),
        )
    }

    fun parseSignUri(uri: Uri): WebEidSignRequest {
        val request = decodeUriFragment(uri)
        val responseUri = validateResponseUri(request.optString("response_uri", ""))
        val hash = request.optString("hash", "")
        val hashFunction = request.optString("hash_function", "")

        if (hash.isBlank() || hashFunction.isBlank()) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Invalid signing request: missing hash or hash_function",
                responseUri.toString(),
            )
        }

        validateAndDecodeHash(
            hashBase64 = hash,
            hashFunction = hashFunction,
            responseUri = responseUri.toString(),
        )

        val signingCertificatePem = request.optString("signing_certificate", "")
        if (signingCertificatePem.isBlank()) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Invalid signing request: missing signing_certificate",
                responseUri.toString(),
            )
        }

        val signingCertificateDerBytes = Base64.getDecoder().decode(signingCertificatePem)
        val signingCertificate = CertificateUtil.x509Certificate(signingCertificateDerBytes)

        return WebEidSignRequest(
            responseUri = responseUri.toString(),
            origin = parseOrigin(responseUri),
            signingCertificate,
            hash = hash,
            hashFunction = hashFunction,
        )
    }

    private fun validateResponseUri(responseUri: String): URI {
        try {
            val uri = URI(responseUri)
            if (uri.scheme.isNullOrBlank()) {
                throw IllegalArgumentException("Invalid response URI scheme")
            }
            if (!uri.scheme.equals("https", ignoreCase = true)) {
                throw IllegalArgumentException("Response URI must use HTTPS scheme")
            }
            if (uri.host.isNullOrBlank()) {
                throw IllegalArgumentException("Invalid response URI host")
            }
            if (uri.userInfo != null) {
                throw IllegalArgumentException("Response URI must not contain userinfo")
            }
            return uri
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException("Invalid response URI", e)
        }
    }

    private fun decodeUriFragment(uri: Uri): JSONObject {
        try {
            val fragment =
                uri.fragment ?: throw IllegalArgumentException("Missing URI fragment")
            val decoded = String(Base64.getDecoder().decode(fragment))
            return JSONObject(decoded)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URI fragment", e)
        }
    }

    private fun parseOrigin(uri: URI): String {
        val portPart = if (uri.port != -1) ":${uri.port}" else ""
        val origin = "${uri.scheme}://${uri.host}$portPart"
        if (origin.length > MAX_ORIGIN_LENGTH) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Invalid origin length",
                uri.toString(),
            )
        }
        return origin
    }

    private fun validateAndDecodeHash(
        hashBase64: String,
        hashFunction: String,
        responseUri: String,
    ): ByteArray {
        val hashBytes =
            try {
                Base64.getDecoder().decode(hashBase64)
            } catch (_: IllegalArgumentException) {
                throw WebEidException(
                    ERR_WEBEID_MOBILE_INVALID_REQUEST,
                    "Invalid hash encoding",
                    responseUri,
                )
            }

        if (hashFunction.length > 8) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "hashFunction value is invalid",
                responseUri,
            )
        }

        val expectedLength =
            try {
                when (hashFunction.uppercase()) {
                    "SHA-224", "SHA3-224" -> 28
                    "SHA-256", "SHA3-256" -> 32
                    "SHA-384", "SHA3-384" -> 48
                    "SHA-512", "SHA3-512" -> 64
                    else -> throw IllegalArgumentException()
                }
            } catch (_: Exception) {
                throw WebEidException(
                    ERR_WEBEID_MOBILE_INVALID_REQUEST,
                    "Unsupported hashFunction: $hashFunction",
                    responseUri,
                )
            }

        if (hashBytes.size != expectedLength) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "$hashFunction hash must be $expectedLength bytes long, but is ${hashBytes.size}",
                responseUri,
            )
        }

        return hashBytes
    }
}
