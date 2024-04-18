@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import android.content.res.AssetManager
import ee.ria.DigiDoc.configuration.utils.Constant.CENTRAL_CONFIGURATION_SERVICE_URL_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_DOWNLOAD_DATE_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_UPDATE_INTERVAL_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_VERSION_SERIAL_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_UPDATE_INTERVAL
import ee.ria.DigiDoc.configuration.utils.Constant.PROPERTIES_FILE_NAME
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

class ConfigurationProperties(assetManager: AssetManager) {
    @Suppress("PropertyName")
    private val LOG_TAG = javaClass.simpleName
    private val dateFormat: SimpleDateFormat
    private lateinit var properties: Properties

    init {
        try {
            assetManager.open("config/$PROPERTIES_FILE_NAME").use { propertiesStream ->
                loadProperties(
                    propertiesStream,
                )
            }
        } catch (e: IOException) {
            throw IllegalStateException(
                "Failed to open $PROPERTIES_FILE_NAME file from assets",
                e,
            )
        }
        dateFormat = DateUtil.dateFormat
    }

    val configurationVersionSerial: Int
        get() = properties.getProperty(CONFIGURATION_VERSION_SERIAL_PROPERTY).toInt()
    val centralConfigurationServiceUrl: String
        get() = properties.getProperty(CENTRAL_CONFIGURATION_SERVICE_URL_PROPERTY)
    val configurationUpdateInterval: Int
        get() =
            try {
                properties.getProperty(CONFIGURATION_UPDATE_INTERVAL_PROPERTY).toInt()
            } catch (nfe: NumberFormatException) {
                errorLog(LOG_TAG, "Unable to get configuration update interval", nfe)
                DEFAULT_UPDATE_INTERVAL
            }
    val packagedConfigurationInitialDownloadDate: Date
        get() {
            val property = properties.getProperty(CONFIGURATION_DOWNLOAD_DATE_PROPERTY)
            return try {
                dateFormat.parse(property) as Date
            } catch (e: ParseException) {
                throw IllegalStateException(
                    "Failed to parse configuration initial download date",
                    e,
                )
            }
        }

    private fun loadProperties(inputStream: InputStream) {
        try {
            properties = Properties()
            properties.load(inputStream)
        } catch (e: IOException) {
            throw IllegalStateException(
                "Failed to load properties $PROPERTIES_FILE_NAME file from assets",
                e,
            )
        }
    }
}
