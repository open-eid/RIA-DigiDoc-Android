@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.secure

import android.content.Context
import android.view.WindowManager
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.MainActivity
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.preferences.DataStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecureUtilTest {
    private lateinit var context: Context

    private lateinit var dataStore: DataStore

    private lateinit var secureUtil: SecureUtil

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStore = DataStore(context)
        secureUtil = SecureUtil(dataStore)
    }

    @Test
    fun secureUtilTest_markAsSecure_activityIsNullReturn() {
        secureUtil.markAsSecure(null)

        // No exception should be thrown, and nothing should happen
    }

    @Test
    fun secureUtilTest_markAsSecure_successMarkAsSecure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        val resources = context.resources
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        preferences.edit {
            putBoolean(
                resources.getString(R.string.main_settings_allow_screenshots_key),
                true,
            )
        }

        scenario.onActivity { activity ->
            secureUtil.markAsSecure(activity)

            val flags = activity.window.attributes.flags
            assertFalse(flags and WindowManager.LayoutParams.FLAG_SECURE != 0)
        }
    }

    @Test
    fun secureUtilTest_markAsSecure_successClearSecure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        val resources = context.resources
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        preferences.edit {
            putBoolean(
                resources.getString(R.string.main_settings_allow_screenshots_key),
                false,
            )
        }

        scenario.onActivity { activity ->
            secureUtil.markAsSecure(activity)

            val flags = activity.window.attributes.flags
            assertTrue(flags and WindowManager.LayoutParams.FLAG_SECURE != 0)
        }
    }
}
