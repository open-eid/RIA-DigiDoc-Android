@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationManager
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationRepositoryImpl
    @Inject
    constructor() : ConfigurationRepository {
        override fun getConfiguration(): ConfigurationProvider? {
            return ConfigurationManager.getConfigurationFlow().value
        }

        @Throws(Exception::class)
        override suspend fun getCentralConfiguration(context: Context): ConfigurationProvider? {
            ConfigurationLoader.loadCentralConfiguration(context)
            return getConfiguration()
        }
    }
