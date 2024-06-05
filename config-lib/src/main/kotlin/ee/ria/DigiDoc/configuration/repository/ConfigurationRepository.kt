@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationProvider

interface ConfigurationRepository {
    fun getConfiguration(): ConfigurationProvider?

    suspend fun getCentralConfiguration(context: Context): ConfigurationProvider?
}
