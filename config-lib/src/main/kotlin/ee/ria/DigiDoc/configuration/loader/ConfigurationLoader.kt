@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.domain.model.ConfigurationData
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import kotlinx.coroutines.flow.StateFlow

interface ConfigurationLoader {
    suspend fun initConfiguration(context: Context)

    fun getConfigurationFlow(): StateFlow<ConfigurationProvider?>

    suspend fun loadConfigurationProperty(context: Context): ConfigurationProperty

    suspend fun loadCachedConfiguration(
        context: Context,
        afterCentralCheck: Boolean,
    )

    suspend fun loadDefaultConfiguration(context: Context)

    @Throws(Exception::class)
    suspend fun loadCentralConfigurationData(
        configurationServiceUrl: String,
        userAgent: String,
    ): ConfigurationData

    @Throws(Exception::class)
    suspend fun loadCentralConfiguration(context: Context)

    suspend fun shouldCheckForUpdates(context: Context): Boolean
}
