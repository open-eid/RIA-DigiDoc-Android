@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import ee.ria.DigiDoc.configuration.utils.SignatureVerifier

interface ConfigurationSignatureVerifier {
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
        val signatureValid: Boolean = SignatureVerifier.verify(signature, publicKey, config)
        check(signatureValid) { "Configuration signature validation failed!" }
    }
}
