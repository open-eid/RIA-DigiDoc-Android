@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoggingUtilsTest {
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        resources = context.resources
    }

    @Test
    fun loggingUtils_isLoggingEnabled_enabledTrueAndRunningTrueReturnTrue() {
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                true,
            )
            .apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                true,
            )
            .apply()

        val result = isLoggingEnabled(context)

        assertTrue(result)
    }

    @Test
    fun loggingUtils_isLoggingEnabled_enabledTrueAndRunningFalseReturnFalse() {
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                true,
            )
            .apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                false,
            )
            .apply()

        val result = isLoggingEnabled(context)

        assertFalse(result)
    }

    @Test
    fun loggingUtils_isLoggingEnabled_enabledFalseAndRunningTrueReturnFalse() {
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                false,
            )
            .apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                true,
            )
            .apply()

        val result = isLoggingEnabled(context)

        assertFalse(result)
    }

    @Test
    fun loggingUtils_isLoggingEnabled_enabledFalseAndRunningFalseReturnFalse() {
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                false,
            )
            .apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                false,
            )
            .apply()

        val result = isLoggingEnabled(context)

        assertFalse(result)
    }
}
