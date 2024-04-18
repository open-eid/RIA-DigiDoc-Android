@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_INFO_FILE_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_UPDATE_DATE_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

class CachedConfigurationHandler(private val cacheDir: File) {
    @Suppress("PropertyName")
    private val LOG_TAG = javaClass.simpleName
    private val dateFormat: SimpleDateFormat
    private var properties: Properties? = null

    init {
        loadProperties()
        if (properties == null) {
            // Cached properties file missing, generating a empty one
            cacheFile(CONFIGURATION_INFO_FILE_NAME, "")
            loadProperties()
            checkNotNull(properties) { "Failed to load properties file $CONFIGURATION_INFO_FILE_NAME" }
        }
        dateFormat = DateUtil.dateFormat
    }

    val configurationVersionSerial: Int?
        get() =
            try {
                val versionSerial = loadProperty(CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME)
                versionSerial?.toInt()
            } catch (nfe: NumberFormatException) {
                errorLog(LOG_TAG, "Unable to get configuration version serial", nfe)
                null
            }
    val confLastUpdateCheckDate: Date?
        get() = loadPropertyDate(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME)
    val confUpdateDate: Date?
        get() = loadPropertyDate(CONFIGURATION_UPDATE_DATE_PROPERTY_NAME)

    private fun loadPropertyDate(propertyName: String): Date? {
        try {
            val property = loadProperty(propertyName)
            return if (property != null) {
                dateFormat.parse(property)
            } else {
                null
            }
        } catch (e: ParseException) {
            throw IllegalStateException("Failed to parse configuration update date", e)
        }
    }

    private fun loadProperty(propertyName: String): String? {
        return properties?.getProperty(propertyName)
    }

    fun cacheFile(
        fileName: String,
        content: String?,
    ) {
        FileUtil.storeFile(cacheDir(fileName), content)
    }

    fun cacheFile(
        fileName: String,
        content: ByteArray?,
    ) {
        FileUtil.storeFile(cacheDir(fileName), content)
    }

    fun updateConfigurationUpdatedDate(date: Date?) {
        properties?.setProperty(
            CONFIGURATION_UPDATE_DATE_PROPERTY_NAME,
            date?.let { dateFormat.format(it) },
        )
    }

    fun updateConfigurationLastCheckDate(date: Date?) {
        properties?.setProperty(
            CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME,
            date?.let { dateFormat.format(it) },
        )
    }

    fun updateConfigurationVersionSerial(versionSerial: Int) {
        properties?.setProperty(
            CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME,
            versionSerial.toString(),
        )
    }

    fun readFileContent(filename: String): String {
        return FileUtil.readFileContent(cacheDir(filename))
    }

    fun readFileContentBytes(filename: String): ByteArray {
        return FileUtil.readFileContentBytes(cacheDir(filename))
    }

    fun doesCachedConfigurationExist(): Boolean {
        return doesCachedConfigurationFileExists(CACHED_CONFIG_JSON)
    }

    fun doesCachedConfigurationFileExists(fileName: String): Boolean {
        val file = File(cacheDir(fileName))
        return file.exists()
    }

    private fun loadProperties() {
        try {
            FileInputStream(cacheDir(CONFIGURATION_INFO_FILE_NAME)).use { fileInputStream ->
                properties = Properties()
                properties?.load(fileInputStream)
            }
        } catch (e: IOException) {
            debugLog(LOG_TAG, "Cached properties file '$CONFIGURATION_INFO_FILE_NAME' not found", e)
            properties = null
        }
    }

    fun updateProperties() {
        try {
            FileOutputStream(cacheDir(CONFIGURATION_INFO_FILE_NAME)).use { fileOutputStream ->
                properties?.store(
                    fileOutputStream,
                    null,
                )
            }
        } catch (e: IOException) {
            throw IllegalStateException(
                "Failed to update properties file '$CONFIGURATION_INFO_FILE_NAME'",
                e,
            )
        }
    }

    private fun cacheDir(filename: String): String {
        return cacheDir.toString() + CACHE_CONFIG_FOLDER + filename
    }
}
