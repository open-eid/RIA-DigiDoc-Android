// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import ee.ria.DigiDoc.configuration.shared.TestConfigurationFiles
import junit.framework.TestCase
import org.junit.Assert.assertThrows
import org.junit.Test

class SignatureVerifierTest {
    @Test
    fun verifyValidSignature() {
        TestCase.assertTrue(SignatureVerifier.verify(signature(), publicKey(), config()))
    }

    @Test
    fun verifyInvalidSignature() {
        TestCase.assertFalse(SignatureVerifier.verify(signature(), publicKey(), config() + "a"))
    }

    @Test
    fun verifyDoesNotAcceptGarbageSignatureBytes() {
        val accepted =
            try {
                SignatureVerifier.verify(byteArrayOf(1, 2, 3, 4), publicKey(), config())
            } catch (_: IllegalStateException) {
                false
            }

        TestCase.assertFalse(accepted)
    }

    @Test
    fun verifyThrowsIllegalStateWhenPublicKeyPemIsEmpty() {
        assertThrows(IllegalStateException::class.java) {
            SignatureVerifier.verify(ByteArray(0), "", "content")
        }
    }

    @Test
    fun verifyThrowsIllegalStateWhenPublicKeyPemIsNotPem() {
        assertThrows(IllegalStateException::class.java) {
            SignatureVerifier.verify(ByteArray(0), "not a pem at all", "content")
        }
    }

    @Test
    fun verifyThrowsIllegalStateWhenPublicKeyPemBodyIsGarbage() {
        val garbagePem =
            "-----BEGIN PUBLIC KEY-----\nbm90IGEgcHVibGljIGtleQ==\n-----END PUBLIC KEY-----\n"
        assertThrows(IllegalStateException::class.java) {
            SignatureVerifier.verify(ByteArray(0), garbagePem, "content")
        }
    }

    private fun config(): String = TestConfigurationFiles.config()

    private fun publicKey(): String = TestConfigurationFiles.publicKey()

    private fun signature(): ByteArray = TestConfigurationFiles.signature()
}
