@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.properties

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import java.util.Date

interface ConfigurationProperties {
    fun getConfigurationProperties(context: Context): ConfigurationProperty

    fun updateProperties(
        context: Context,
        lastUpdateCheck: Date?,
        lastUpdated: Date?,
        serial: Int?,
    )

    fun getConfigurationUpdatedDate(context: Context): Date?

    fun setConfigurationUpdatedDate(
        context: Context,
        date: Date?,
    )

    fun getConfigurationLastCheckDate(context: Context): Date?

    fun setConfigurationLastCheckDate(
        context: Context,
        date: Date?,
    )

    fun getConfigurationVersionSerial(context: Context): Int?

    fun setConfigurationVersionSerial(
        context: Context,
        serial: Int?,
    )
}
