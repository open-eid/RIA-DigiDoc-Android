@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationManager
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties.getConfigurationLastCheckDate
import kotlinx.coroutines.flow.filterNotNull
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object ConfigurationUtil {
    @Throws(Exception::class)
    suspend fun initConfiguration(context: Context) {
        ConfigurationManager.loadConfiguration(context)
    }

    suspend fun observeConfigurationUpdates(onUpdate: (ConfigurationProvider) -> Unit) {
        ConfigurationManager.getConfigurationFlow()
            .filterNotNull()
            .collect { config ->
                onUpdate(config)
            }
    }

    fun isSerialNewerThanCached(
        cachedSerial: Int?,
        newSerial: Int,
    ): Boolean {
        return when {
            cachedSerial == null -> true
            else -> {
                newSerial > cachedSerial
            }
        }
    }

    fun shouldCheckForUpdates(context: Context): Boolean {
        val lastExecutionDate =
            getConfigurationLastCheckDate(context)?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDateTime()

        val currentDate = LocalDateTime.now()

        val daysSinceLastUpdateCheck = ChronoUnit.DAYS.between(lastExecutionDate, currentDate)

        return daysSinceLastUpdateCheck >= 4
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun isBase64(encoded: String): Boolean {
        return try {
            Base64.decode(encoded)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
