@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import android.content.Context
import android.content.SharedPreferences
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_PREFERENCES
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_UPDATE_DATE_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class ConfigurationPropertiesTest {
    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    lateinit var mockEditor: SharedPreferences.Editor

    lateinit var configurationProperties: ConfigurationProperties

    @Before
    fun setup() {
        configurationProperties = ConfigurationPropertiesImpl()
        `when`(
            mockContext.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE),
        ).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        doAnswer {
            mockEditor
        }.`when`(mockEditor).apply()
    }

    @Test
    fun configurationProperties_updateProperties_updatingAllPropertiesSuccess() {
        val lastUpdateCheck = Date()
        val lastUpdated = Date()
        val serial = 123

        val expectedLastUpdateDate = DateUtil.dateFormat.format(lastUpdateCheck)
        val expectedLastUpdatedDate = DateUtil.dateFormat.format(lastUpdated)

        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)

        `when`(mockSharedPreferences.contains(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME)).thenReturn(true)
        `when`(mockSharedPreferences.contains(CONFIGURATION_UPDATE_DATE_PROPERTY_NAME)).thenReturn(true)
        `when`(mockSharedPreferences.contains(CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME)).thenReturn(true)

        `when`(
            mockSharedPreferences.getString(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME, null),
        ).thenReturn(expectedLastUpdateDate)
        `when`(
            mockSharedPreferences.getString(CONFIGURATION_UPDATE_DATE_PROPERTY_NAME, null),
        ).thenReturn(expectedLastUpdatedDate)
        `when`(mockSharedPreferences.getInt(CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME, 0)).thenReturn(serial)

        configurationProperties.updateProperties(mockContext, lastUpdateCheck, lastUpdated, serial)

        val expectedLastUpdateDateString = DateUtil.stringToDate(expectedLastUpdateDate)
        val expectedLastUpdatedDateString = DateUtil.stringToDate(expectedLastUpdatedDate)

        assertEquals(expectedLastUpdateDateString, configurationProperties.getConfigurationLastCheckDate(mockContext))
        assertEquals(expectedLastUpdatedDateString, configurationProperties.getConfigurationUpdatedDate(mockContext))
        assertEquals(serial, configurationProperties.getConfigurationVersionSerial(mockContext))
    }

    @Test
    fun configurationProperties_updateProperties_updatingUpdatedDateSuccess() {
        val currentDate = Date()
        val expectedDate = DateUtil.dateFormat.format(currentDate)

        `when`(mockSharedPreferences.contains(CONFIGURATION_UPDATE_DATE_PROPERTY_NAME)).thenReturn(true)
        `when`(mockSharedPreferences.getString(CONFIGURATION_UPDATE_DATE_PROPERTY_NAME, null)).thenReturn(expectedDate)

        val dateString = DateUtil.stringToDate(expectedDate)
        assertEquals(dateString, configurationProperties.getConfigurationUpdatedDate(mockContext))
    }

    @Test
    fun configurationProperties_updateProperties_updatingLastCheckDateSuccess() {
        val currentDate = Date()
        val expectedDate = DateUtil.dateFormat.format(currentDate)

        `when`(mockSharedPreferences.contains(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME)).thenReturn(true)
        `when`(
            mockSharedPreferences.getString(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME, null),
        ).thenReturn(expectedDate)

        val dateString = DateUtil.stringToDate(expectedDate)
        assertEquals(dateString, configurationProperties.getConfigurationLastCheckDate(mockContext))
    }

    @Test
    fun configurationProperties_updateProperties_updatingVersionSerialSuccess() {
        val serial = 123

        `when`(mockSharedPreferences.contains(CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME)).thenReturn(true)
        `when`(mockSharedPreferences.getInt(CONFIGURATION_VERSION_SERIAL_PROPERTY_NAME, 0)).thenReturn(serial)

        assertEquals(serial, configurationProperties.getConfigurationVersionSerial(mockContext))
    }
}
