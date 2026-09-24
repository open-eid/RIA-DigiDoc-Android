// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

object Constant {
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
    const val DEFAULT_CONFIG_ECC = "default-config.ecc"
    const val DEFAULT_CONFIG_ECPUB = "default-config.ecpub"

    const val CACHED_CONFIG_JSON = "active-config.json"
    const val CACHED_CONFIG_ECC = "active-config.ecc"

    const val CONFIGURATION_PREFERENCES = "ConfigurationPreferences"
    const val CACHE_CONFIG_FOLDER = "/config/"
    const val CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME =
        "configuration.last-update-check-date"
    const val CONFIGURATION_UPDATE_DATE_PROPERTY_NAME = "configuration.update-date"
    const val CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME = "configuration.version-serial"
    const val CONFIGURATION_LAST_CHECKED_APP_VERSION_PROPERTY_NAME =
        "configuration.last-checked-app-version"
}
