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
