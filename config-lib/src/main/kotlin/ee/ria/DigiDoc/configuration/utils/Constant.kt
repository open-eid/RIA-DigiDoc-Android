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

package ee.ria.DigiDoc.configuration.utils

object Constant {
    const val FORCE_LOAD_CENTRAL_CONFIGURATION =
        "ee.ria.digidoc.configuration.FORCE_LOAD_CENTRAL_CONFIGURATION"
    const val LAST_CONFIGURATION_UPDATE = "ee.ria.digidoc.configuration.LAST_CONFIGURATION_UPDATE"

    const val CENTRAL_CONFIGURATION_SERVICE_URL_PROPERTY = "central-configuration-service.url"
    const val CONFIGURATION_UPDATE_INTERVAL_PROPERTY = "configuration.update-interval"
    const val CONFIGURATION_VERSION_SERIAL_PROPERTY = "configuration.version-serial"
    const val CONFIGURATION_DOWNLOAD_DATE_PROPERTY = "configuration.download-date"
    const val PROPERTIES_FILE_NAME = "configuration.properties"
    const val DEFAULT_UPDATE_INTERVAL = 4

    const val CENTRAL_CONF_SERVICE_URL_NAME = "central-configuration-service.url"
    const val DEFAULT_CONFIGURATION_PROPERTIES_FILE_NAME =
        "default-configuration.properties"

    const val DEFAULT_CONFIG_JSON = "default-config.json"
    const val DEFAULT_CONFIG_RSA = "default-config.rsa"
    const val DEFAULT_CONFIG_PUB = "default-config.pub"

    const val CACHED_CONFIG_JSON = "active-config.json"
    const val CACHED_CONFIG_RSA = "active-config.rsa"
    const val CACHED_CONFIG_PUB = "active-config.pub"

    const val CONFIGURATION_PREFERENCES = "ConfigurationPreferences"
    const val CACHE_CONFIG_FOLDER = "/config/"
    const val CONFIGURATION_INFO_FILE_NAME = "configuration-info.properties"
    const val CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME =
        "configuration.last-update-check-date"
    const val CONFIGURATION_UPDATE_DATE_PROPERTY_NAME = "configuration.update-date"
    const val CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME = "configuration.version-serial"
}
