@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.domain.model

data class ConfigurationData(
    val configurationJson: String?,
    val configurationSignaturePublicKey: String?,
    val configurationSignature: ByteArray?,
)
