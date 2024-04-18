@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

abstract class ConfigurationLoader {
    var configurationJson: String? = null
    var configurationSignature: ByteArray? = null
    var configurationSignaturePublicKey: String? = null

    fun load() {
        configurationJson = loadConfigurationJson()?.trim { it <= ' ' }
        assertConfigurationJson()
        configurationSignature = loadConfigurationSignature()
        assertConfigurationSignature()
        configurationSignaturePublicKey = loadConfigurationSignaturePublicKey()?.trim { it <= ' ' }
        assertConfigurationSignaturePublicKey()
    }

    fun assertConfigurationJson() {
        assertValueNotBlank(configurationJson, "configuration json")
    }

    fun assertConfigurationSignature() {
        check(!(configurationSignature == null || configurationSignature!!.isEmpty())) {
            "Loaded configuration signature file is blank"
        }
    }

    fun assertConfigurationSignaturePublicKey() {
        assertValueNotBlank(configurationSignaturePublicKey, "configuration signature public key")
    }

    abstract fun loadConfigurationJson(): String?

    abstract fun loadConfigurationSignature(): ByteArray?

    abstract fun loadConfigurationSignaturePublicKey(): String?

    private fun assertValueNotBlank(
        value: String?,
        valueName: String,
    ) {
        check(!value.isNullOrEmpty()) { "Loaded $valueName file is blank" }
    }
}
