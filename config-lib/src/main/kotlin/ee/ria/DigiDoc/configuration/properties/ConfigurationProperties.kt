@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.properties

import android.content.Context
import ee.ria.DigiDoc.configuration.utils.Constant
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_PREFERENCES
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import java.util.Date

object ConfigurationProperties {
    fun updateProperties(
        context: Context,
        lastUpdateCheck: Date?,
        lastUpdated: Date?,
        serial: Int?,
    ) {
        setConfigurationLastCheckDate(context, lastUpdateCheck)
        setConfigurationUpdatedDate(context, lastUpdated)
        setConfigurationVersionSerial(context, serial)
    }

    private fun setConfigurationUpdatedDate(
        context: Context,
        date: Date?,
    ) {
        val updateDatePropertyName = Constant.CONFIGURATION_UPDATE_DATE_PROPERTY_NAME
        val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(updateDatePropertyName, DateUtil.dateFormat.format(date ?: Date()))
            .apply()
    }

    fun getConfigurationUpdatedDate(context: Context): Date? {
        val updateDatePropertyName = Constant.CONFIGURATION_UPDATE_DATE_PROPERTY_NAME
        val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(updateDatePropertyName)) {
            return sharedPreferences.getString(updateDatePropertyName, null)
                ?.let { DateUtil.stringToDate(it) }
        }
        return null
    }

    private fun setConfigurationLastCheckDate(
        context: Context,
        date: Date?,
    ) {
        val lastUpdateDateCheckDatePropertyName =
            Constant.CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
        val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(lastUpdateDateCheckDatePropertyName, DateUtil.dateFormat.format(date ?: Date()))
            .apply()
    }

    fun getConfigurationLastCheckDate(context: Context): Date? {
        val lastUpdateDateCheckDatePropertyName =
            Constant.CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
        val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(lastUpdateDateCheckDatePropertyName)) {
            return sharedPreferences.getString(lastUpdateDateCheckDatePropertyName, null)
                ?.let { DateUtil.stringToDate(it) }
        }
        return null
    }

    private fun setConfigurationVersionSerial(
        context: Context,
        serial: Int?,
    ) {
        val configurationVersionSerialPropertyName =
            Constant.CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME
        val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt(configurationVersionSerialPropertyName, serial ?: 0)
            .apply()
    }

    fun getConfigurationVersionSerial(context: Context): Int? {
        val configurationVersionSerialPropertyName =
            Constant.CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME
        val sharedPreferences = context.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(configurationVersionSerialPropertyName)) {
            return sharedPreferences.getInt(configurationVersionSerialPropertyName, 0)
        }
        return null
    }
}
