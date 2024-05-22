@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationManager
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader

class ConfigurationRepository {
    fun getConfiguration(): ConfigurationProvider? {
        return ConfigurationManager.getConfigurationFlow().value
    }

    @Throws(Exception::class)
    suspend fun getCentralConfiguration(context: Context): ConfigurationProvider? {
        ConfigurationLoader.loadCentralConfiguration(context)
        return getConfiguration()
    }
}
