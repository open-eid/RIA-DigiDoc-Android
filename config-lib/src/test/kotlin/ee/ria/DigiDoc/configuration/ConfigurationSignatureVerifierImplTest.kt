// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import ee.ria.DigiDoc.configuration.exception.ConfigurationSignatureValidationException
import ee.ria.DigiDoc.configuration.shared.TestConfigurationFiles
import org.junit.Assert.assertThrows
import org.junit.Test

class ConfigurationSignatureVerifierImplTest {
    private val verifier = ConfigurationSignatureVerifierImpl()

    @Test
    fun verifyConfigurationSignatureSucceedsForMatchingSignature() {
        verifier.verifyConfigurationSignature(config(), publicKey(), signature())
    }

    @Test
    fun verifyConfigurationSignatureThrowsValidationExceptionWhenConfigTampered() {
        assertThrows(ConfigurationSignatureValidationException::class.java) {
            verifier.verifyConfigurationSignature(config() + "a", publicKey(), signature())
        }
    }

    @Test
    fun verifyConfigurationSignatureThrowsValidationExceptionWhenSignatureCorrupted() {
        val corrupted = signature().copyOf()
        corrupted[0] = (corrupted[0] + 1).toByte()

        assertThrows(ConfigurationSignatureValidationException::class.java) {
            verifier.verifyConfigurationSignature(config(), publicKey(), corrupted)
        }
    }

    @Test
    fun verifyConfigurationSignatureThrowsValidationExceptionWhenSignatureEmpty() {
        assertThrows(ConfigurationSignatureValidationException::class.java) {
            verifier.verifyConfigurationSignature(config(), publicKey(), ByteArray(0))
        }
    }

    @Test
    fun verifyConfigurationSignatureThrowsValidationExceptionWhenPublicKeyMalformed() {
        assertThrows(ConfigurationSignatureValidationException::class.java) {
            verifier.verifyConfigurationSignature(config(), "not a pem", signature())
        }
    }

    private fun config(): String = TestConfigurationFiles.config()

    private fun publicKey(): String = TestConfigurationFiles.publicKey()

    private fun signature(): ByteArray = TestConfigurationFiles.signature()
}
