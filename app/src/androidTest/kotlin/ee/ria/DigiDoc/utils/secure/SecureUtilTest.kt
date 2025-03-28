@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.secure

import android.view.Window
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.R
import org.junit.Test
import org.mockito.kotlin.mock

class SecureUtilTest {
    @Test
    fun secureUtilTest_markAsSecure_successMarkAsSecure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resources = context.resources
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        preferences.edit {
            putBoolean(
                resources.getString(R.string.main_settings_allow_screenshots_key),
                true,
            )
        }

        val window: Window = mock()

        SecureUtil.markAsSecure(context, window)
    }

    @Test
    fun secureUtilTest_markAsSecure_successClearSecure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resources = context.resources
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        preferences.edit {
            putBoolean(
                resources.getString(R.string.main_settings_allow_screenshots_key),
                false,
            )
        }

        val window: Window = mock()

        SecureUtil.markAsSecure(context, window)
    }
}
