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

        val expectedLastUpdateDate = DateUtil.dateTimeFormat.format(lastUpdateCheck)
        val expectedLastUpdatedDate = DateUtil.dateTimeFormat.format(lastUpdated)

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
        val expectedDate = DateUtil.dateTimeFormat.format(currentDate)

        `when`(mockSharedPreferences.contains(CONFIGURATION_UPDATE_DATE_PROPERTY_NAME)).thenReturn(true)
        `when`(mockSharedPreferences.getString(CONFIGURATION_UPDATE_DATE_PROPERTY_NAME, null)).thenReturn(expectedDate)

        val dateString = DateUtil.stringToDate(expectedDate)
        assertEquals(dateString, configurationProperties.getConfigurationUpdatedDate(mockContext))
    }

    @Test
    fun configurationProperties_updateProperties_updatingLastCheckDateSuccess() {
        val currentDate = Date()
        val expectedDate = DateUtil.dateTimeFormat.format(currentDate)

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
