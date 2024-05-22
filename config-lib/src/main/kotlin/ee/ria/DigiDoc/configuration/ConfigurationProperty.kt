@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ConfigurationProperty(
    val centralConfigurationServiceUrl: String,
    val updateInterval: Int,
    val versionSerial: Int,
    val downloadDate: LocalDateTime,
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        fun fromProperties(properties: Map<String, String>): ConfigurationProperty {
            val url =
                properties["central-configuration-service.url"]
                    ?: throw IllegalArgumentException("Configuration property URL is missing")
            val updateInterval =
                properties["configuration.update-interval"]?.toInt()
                    ?: throw IllegalArgumentException("Configuration property update interval is missing")
            val versionSerial =
                properties["configuration.version-serial"]?.toInt()
                    ?: throw IllegalArgumentException("Configuration property version serial is missing")
            val downloadDate =
                properties["configuration.download-date"]?.let {
                    LocalDateTime.parse(it, DATE_FORMATTER)
                } ?: throw IllegalArgumentException("Configuration property download date is missing or invalid")

            return ConfigurationProperty(
                centralConfigurationServiceUrl = url,
                updateInterval = updateInterval,
                versionSerial = versionSerial,
                downloadDate = downloadDate,
            )
        }
    }
}
