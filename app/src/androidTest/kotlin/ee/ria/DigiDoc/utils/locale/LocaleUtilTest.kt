// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.locale

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.LocaleList
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.KEY_LOCALE
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
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

    @Test
    fun localeUtil_createLocalizedContext_usesSelectedLanguageOverDeviceLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val result =
            localeUtil.createLocalizedContext(context, getLocale("en"), LocaleList.forLanguageTags("et-EE"))

        assertEnglish(result)
    }

    @Test
    fun localeUtil_createLocalizedContext_usesSelectedEstonianOnEnglishDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val result =
            localeUtil.createLocalizedContext(context, getLocale("et"), LocaleList.forLanguageTags("en-US"))

        assertEstonian(result)
    }

    @Test
    fun localeUtil_createLocalizedContext_usesDeviceLanguageWhenNoneSelected() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val result = localeUtil.createLocalizedContext(context, null, LocaleList.forLanguageTags("et-EE"))

        assertEstonian(result)
    }

    @Test
    fun localeUtil_createLocalizedContext_usesFirstSupportedPreferredDeviceLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val result = localeUtil.createLocalizedContext(context, null, LocaleList.forLanguageTags("fi-FI,et-EE"))

        assertEstonian(result)
    }

    @Test
    fun localeUtil_createLocalizedContext_fallsBackToEnglishForUnsupportedDeviceLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val result = localeUtil.createLocalizedContext(context, null, LocaleList.forLanguageTags("fi-FI"))

        assertEnglish(result)
    }

    private fun assertEstonian(context: Context) {
        assertTrue(
            context
                .getString(R.string.unsupported_version_message, "www.id.ee")
                .startsWith("Sinu nutiseadmes olev ID-tarkvara"),
        )
        assertTrue(context.getString(R.string.rooted_device).startsWith("Seda rakendust ei saa kasutada"))
    }

    private fun assertEnglish(context: Context) {
        assertTrue(
            context
                .getString(R.string.unsupported_version_message, "www.id.ee")
                .startsWith("This version of ID-software on your smart device"),
        )
        assertTrue(context.getString(R.string.rooted_device).startsWith("This app cannot be used"))
    }
}
