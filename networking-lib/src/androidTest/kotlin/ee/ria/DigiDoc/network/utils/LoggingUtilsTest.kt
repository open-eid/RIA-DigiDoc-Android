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
            ).apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                true,
            ).apply()

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
            ).apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                false,
            ).apply()

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
            ).apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                true,
            ).apply()

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
            ).apply()
        preferences
            .edit()
            .putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                false,
            ).apply()

        val result = isLoggingEnabled(context)

        assertFalse(result)
    }
}
