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
