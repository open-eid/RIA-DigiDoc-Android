// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import ee.ria.DigiDoc.configuration.exception.ConfigurationSignatureValidationException
import ee.ria.DigiDoc.configuration.utils.SignatureVerifier

interface ConfigurationSignatureVerifier {
    @Throws(ConfigurationSignatureValidationException::class)
    fun verifyConfigurationSignature(
        config: String,
        publicKey: String,
        signature: ByteArray,
    )
}

class ConfigurationSignatureVerifierImpl : ConfigurationSignatureVerifier {
    override fun verifyConfigurationSignature(
        config: String,
        publicKey: String,
        signature: ByteArray,
    ) {
        val signatureValid =
            try {
                SignatureVerifier.verify(signature, publicKey, config)
            } catch (e: IllegalStateException) {
                throw ConfigurationSignatureValidationException(e)
            }

        if (!signatureValid) {
            throw ConfigurationSignatureValidationException()
        }
    }
}
