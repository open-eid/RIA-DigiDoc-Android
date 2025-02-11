@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting

interface ConfigurationRepository {
    fun getConfiguration(): ConfigurationProvider?

    @Throws(Exception::class)
    suspend fun getCentralConfiguration(
        proxySetting: ProxySetting?,
        manualProxy: ManualProxy,
    ): ConfigurationProvider?

    suspend fun observeConfigurationUpdates(onUpdate: (ConfigurationProvider) -> Unit)
}
