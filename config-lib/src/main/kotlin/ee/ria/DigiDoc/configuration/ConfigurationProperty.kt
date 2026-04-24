/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Singleton
data class ConfigurationProperty(
    var centralConfigurationServiceUrl: String = "",
    var updateInterval: Int = 4,
    var versionSerial: Int = 0,
    var downloadDate: LocalDateTime = LocalDateTime.now(),
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
