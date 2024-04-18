@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

object Constant {
    const val FORCE_LOAD_CENTRAL_CONFIGURATION =
        "ee.ria.digidoc.configuration.FORCE_LOAD_CENTRAL_CONFIGURATION"
    const val CONFIGURATION_RESULT_RECEIVER =
        "ee.ria.digidoc.configuration.CONFIGURATION_RESULT_RECEIVER"
    const val CONFIGURATION_PROVIDER = "ee.ria.digidoc.configuration.CONFIGURATION_PROVIDER"
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

    const val CACHE_CONFIG_FOLDER = "/config/"
    const val CONFIGURATION_INFO_FILE_NAME = "configuration-info.properties"
    const val CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME =
        "configuration.last-update-check-date"
    const val CONFIGURATION_UPDATE_DATE_PROPERTY_NAME = "configuration.update-date"
    const val CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME = "configuration.version-serial"
}
