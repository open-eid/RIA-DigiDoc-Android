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

package ee.ria.DigiDoc.utils.locale

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.KEY_LOCALE
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.Locale

class LocaleUtilTest {
    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockResources: Resources

    @Mock
    lateinit var mockSharedPreferences: SharedPreferences

    private lateinit var localeUtil: LocaleUtilImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        localeUtil = LocaleUtilImpl()

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.configuration).thenReturn(context.resources.configuration)
    }

    @Test
    fun localeUtil_getPreferredLanguage_success() {
        `when`(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.getString(KEY_LOCALE, null)).thenReturn("et")

        val result = localeUtil.getPreferredLanguage(mockContext)

        assertEquals("et", result)
        verify(mockSharedPreferences).getString(KEY_LOCALE, null)
    }

    @Test
    fun localeUtil_getPreferredLanguage_returnDefaultLanguage() {
        `when`(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.getString(KEY_LOCALE, null)).thenReturn(null)

        val result = localeUtil.getPreferredLanguage(mockContext)

        assertEquals("en", result)
        verify(mockSharedPreferences).getString(KEY_LOCALE, null)
    }

    @Test
    fun localeUtil_updateLocale_success() {
        val newLocale = getLocale("et")

        `when`(mockContext.createConfigurationContext(any())).thenReturn(mockContext)

        val result = localeUtil.updateLocale(mockContext, newLocale)

        assertEquals(result, mockContext)
        assertEquals(Locale.getDefault(), newLocale)
    }
}
