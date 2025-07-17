@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import android.content.Context
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationRepositoryImpl
    @Inject
    constructor(
        private val context: Context,
        private val configurationLoader: ConfigurationLoader,
    ) : ConfigurationRepository {
        override fun getConfiguration(): ConfigurationProvider? = configurationLoader.getConfigurationFlow().value

        @Throws(Exception::class)
        override suspend fun getCentralConfiguration(
            proxySetting: ProxySetting?,
            manualProxy: ManualProxy,
        ): ConfigurationProvider? {
            configurationLoader.loadCentralConfiguration(context, proxySetting, manualProxy)
            return getConfiguration()
        }

        override suspend fun observeConfigurationUpdates(onUpdate: (ConfigurationProvider) -> Unit) {
            configurationLoader
                .getConfigurationFlow()
                .filterNotNull()
                .collect { config ->
                    onUpdate(config)
                }
        }
    }
