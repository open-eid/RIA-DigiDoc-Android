@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_RSA

class CachedConfigurationLoader(confCacheHandler: CachedConfigurationHandler) :
    ConfigurationLoader() {
    private val confCacheHandler: CachedConfigurationHandler

    init {
        this.confCacheHandler = confCacheHandler
    }

    override fun loadConfigurationJson(): String {
        return confCacheHandler.readFileContent(CACHED_CONFIG_JSON)
    }

    override fun loadConfigurationSignature(): ByteArray {
        return confCacheHandler.readFileContentBytes(CACHED_CONFIG_RSA)
    }

    override fun loadConfigurationSignaturePublicKey(): String {
        return confCacheHandler.readFileContent(CACHED_CONFIG_PUB)
    }
}
