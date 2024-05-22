@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import android.content.Context
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader.loadCachedConfiguration
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader.loadCentralConfiguration
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader.loadDefaultConfiguration
import ee.ria.DigiDoc.configuration.utils.ConfigurationUtil.shouldCheckForUpdates
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.jvm.Throws

object ConfigurationManager {
    private val configurationFlow = MutableStateFlow<ConfigurationProvider?>(null)

    fun getConfigurationFlow(): StateFlow<ConfigurationProvider?> = configurationFlow

    fun setConfigurationFlow(configurationProvider: ConfigurationProvider?) =
        run {
            if (configurationProvider != null) {
                configurationFlow.value = configurationProvider
            }
        }

    @Throws(Exception::class)
    suspend fun loadConfiguration(context: Context) {
        val cacheDir = File(context.cacheDir, CACHE_CONFIG_FOLDER)
        if (!cacheDir.exists()) cacheDir.mkdir()

        val currentConfig = loadCachedConfiguration(context) ?: loadDefaultConfiguration(context)

        configurationFlow.value = currentConfig

        if (shouldCheckForUpdates(context)) {
            loadCentralConfiguration(context)
        }
    }
}
