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

package ee.ria.DigiDoc.configuration.utils

import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.openssl.PEMParser
import java.io.IOException
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

object SignatureVerifier {
    private val LOG_TAG = javaClass.simpleName

    private const val CONVERSION_ERROR = "Failed to convert SubjectPublicKeyInfo to EC java.security.PublicKey"
    private const val PARSE_ERROR = "Failed to parse PEM encoded public key"
    private const val NOT_SPKI_ERROR =
        "PEM did not contain SubjectPublicKeyInfo. Make sure it's 'BEGIN PUBLIC KEY' (SPKI)."
    private const val VERIFY_ERROR = "Failed to verify signature"

    fun verify(
        signature: ByteArray,
        publicKeyPEM: String,
        signedContent: String,
    ): Boolean {
        val publicKeyInfo = parsePublicKeyInfo(publicKeyPEM)
        val publicKey = convertPublicKeyInfoToPublicKey(publicKeyInfo)
        return verifySignature(signature, publicKey, signedContent)
    }

    private fun convertPublicKeyInfoToPublicKey(publicKeyInfo: SubjectPublicKeyInfo): PublicKey =
        try {
            val keySpec = X509EncodedKeySpec(publicKeyInfo.encoded)
            KeyFactory.getInstance("EC").generatePublic(keySpec)
        } catch (e: GeneralSecurityException) {
            fail(CONVERSION_ERROR, e)
        } catch (e: IOException) {
            fail(CONVERSION_ERROR, e)
        } catch (e: RuntimeException) {
            fail(CONVERSION_ERROR, e)
        }

    private fun parsePublicKeyInfo(publicKeyPem: String): SubjectPublicKeyInfo {
        val pemObject =
            try {
                PEMParser(StringReader(publicKeyPem)).use { it.readObject() }
            } catch (e: IOException) {
                fail(PARSE_ERROR, e)
            } catch (e: RuntimeException) {
                fail(PARSE_ERROR, e)
            }

        return pemObject as? SubjectPublicKeyInfo ?: fail(NOT_SPKI_ERROR)
    }

    private fun verifySignature(
        signatureBytes: ByteArray,
        publicKey: PublicKey,
        signedContent: String,
    ): Boolean =
        try {
            val signature = Signature.getInstance(pickEcdsaAlgorithm(publicKey))
            signature.initVerify(publicKey)
            signature.update(signedContent.toByteArray(StandardCharsets.UTF_8))
            signature.verify(signatureBytes)
        } catch (e: GeneralSecurityException) {
            fail(VERIFY_ERROR, e)
        } catch (e: IllegalArgumentException) {
            fail(VERIFY_ERROR, e)
        }

    private fun fail(
        message: String,
        cause: Throwable? = null,
    ): Nothing {
        errorLog(LOG_TAG, message, cause)
        throw IllegalStateException(message, cause)
    }

    private fun pickEcdsaAlgorithm(publicKey: PublicKey): String {
        val ecKey =
            publicKey as? ECPublicKey
                ?: fail("Public key is not EC (ECDSA). Got: ${publicKey.algorithm}")

        val fieldSizeBits = ecKey.params.curve.field.fieldSize

        return when {
            fieldSizeBits <= 256 -> "SHA256withECDSA"
            fieldSizeBits <= 384 -> "SHA384withECDSA"
            else -> "SHA512withECDSA"
        }
    }
}
