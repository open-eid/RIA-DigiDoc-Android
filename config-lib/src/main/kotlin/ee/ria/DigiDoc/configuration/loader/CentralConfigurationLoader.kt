@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import android.content.Context
import ee.ria.DigiDoc.network.configuration.client.CentralConfigurationClient
import org.bouncycastle.util.encoders.Base64

class CentralConfigurationLoader(
    context: Context?,
    configurationServiceUrl: String,
    userAgent: String,
) : ConfigurationLoader() {
    private val configurationClient: CentralConfigurationClient

    init {
        configurationClient = CentralConfigurationClient(context, configurationServiceUrl, userAgent)
    }

    override fun loadConfigurationJson(): String? {
        super.configurationJson = configurationClient.configuration.trim()
        assertConfigurationJson()
        return configurationJson
    }

    override fun loadConfigurationSignature(): ByteArray? {
        val trimmedConfigurationSignature: String =
            configurationClient.configurationSignature.trim()
        super.configurationSignature = Base64.decode(trimmedConfigurationSignature)
        assertConfigurationSignature()
        return configurationSignature
    }

    override fun loadConfigurationSignaturePublicKey(): String? {
        super.configurationSignaturePublicKey =
            configurationClient.configurationSignaturePublicKey.trim()
        assertConfigurationSignaturePublicKey()
        return configurationSignaturePublicKey
    }
}
