@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider

interface ConfigurationRepository {
    fun getConfiguration(): ConfigurationProvider?

    @Throws(Exception::class)
    suspend fun getCentralConfiguration(): ConfigurationProvider?

    suspend fun observeConfigurationUpdates(onUpdate: (ConfigurationProvider) -> Unit)
}
