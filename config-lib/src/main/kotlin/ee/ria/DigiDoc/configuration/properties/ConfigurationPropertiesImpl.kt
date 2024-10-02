@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.properties

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_PREFERENCES
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_UPDATE_DATE_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.PROPERTIES_FILE_NAME
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import java.util.Date
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationPropertiesImpl
    @Inject
    constructor() : ConfigurationProperties {
        override fun getConfigurationProperties(context: Context): ConfigurationProperty {
            val properties = Properties()
            context.assets.open("config/${PROPERTIES_FILE_NAME}").use { inputStream ->
                properties.load(inputStream)
            }

            val mappedProperties =
                properties.entries.associate {
                    it.key.toString() to it.value.toString()
                }
            return ConfigurationProperty.fromProperties(mappedProperties)
        }

        override fun updateProperties(
            context: Context,
            lastUpdateCheck: Date?,
            lastUpdated: Date?,
            serial: Int?,
        ) {
            setConfigurationLastCheckDate(context, lastUpdateCheck)
            setConfigurationUpdatedDate(context, lastUpdated)
            setConfigurationVersionSerial(context, serial)
        }

        override fun setConfigurationUpdatedDate(
            context: Context,
            date: Date?,
        ) {
            if (date != null) {
                val updateDatePropertyName = CONFIGURATION_UPDATE_DATE_PROPERTY_NAME
                val sharedPreferences =
                    context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString(updateDatePropertyName, DateUtil.dateTimeFormat.format(date))
                    .apply()
            }
        }

        override fun getConfigurationUpdatedDate(context: Context): Date? {
            val updateDatePropertyName = CONFIGURATION_UPDATE_DATE_PROPERTY_NAME
            val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
            if (sharedPreferences.contains(updateDatePropertyName)) {
                return sharedPreferences.getString(updateDatePropertyName, null)
                    ?.let { DateUtil.stringToDate(it) }
            }
            return null
        }

        override fun setConfigurationLastCheckDate(
            context: Context,
            date: Date?,
        ) {
            if (date != null) {
                val lastUpdateDateCheckDatePropertyName =
                    CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
                val sharedPreferences =
                    context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString(
                        lastUpdateDateCheckDatePropertyName,
                        DateUtil.dateTimeFormat.format(date),
                    )
                    .apply()
            }
        }

        override fun getConfigurationLastCheckDate(context: Context): Date? {
            val lastUpdateDateCheckDatePropertyName =
                CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
            val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
            if (sharedPreferences.contains(lastUpdateDateCheckDatePropertyName)) {
                return sharedPreferences.getString(lastUpdateDateCheckDatePropertyName, null)
                    ?.let { DateUtil.stringToDate(it) }
            }
            return null
        }

        override fun setConfigurationVersionSerial(
            context: Context,
            serial: Int?,
        ) {
            if (serial != null) {
                val configurationVersionSerialPropertyName =
                    CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME
                val sharedPreferences =
                    context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putInt(configurationVersionSerialPropertyName, serial)
                    .apply()
            }
        }

        override fun getConfigurationVersionSerial(context: Context): Int? {
            val configurationVersionSerialPropertyName =
                CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME
            val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
            if (sharedPreferences.contains(configurationVersionSerialPropertyName)) {
                return sharedPreferences.getInt(configurationVersionSerialPropertyName, 0)
            }
            return null
        }
    }
