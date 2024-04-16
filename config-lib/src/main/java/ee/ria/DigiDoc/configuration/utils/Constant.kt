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
}
