@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import ee.ria.DigiDoc.configuration.utils.SignatureVerifier

class ConfigurationSignatureVerifier(private val publicKey: String) {
    fun verifyConfigurationSignature(
        config: String,
        signature: ByteArray,
    ) {
        val signatureValid: Boolean = SignatureVerifier.verify(signature, publicKey, config)
        check(signatureValid) { "Configuration signature validation failed!" }
    }
}
