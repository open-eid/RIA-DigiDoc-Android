@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import android.content.Context
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
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
        override fun getConfiguration(): ConfigurationProvider? {
            return configurationLoader.getConfigurationFlow().value
        }

        @Throws(Exception::class)
        override suspend fun getCentralConfiguration(): ConfigurationProvider? {
            configurationLoader.loadCentralConfiguration(context)
            return getConfiguration()
        }

        override suspend fun observeConfigurationUpdates(onUpdate: (ConfigurationProvider) -> Unit) {
            configurationLoader.getConfigurationFlow()
                .filterNotNull()
                .collect { config ->
                    onUpdate(config)
                }
        }
    }
