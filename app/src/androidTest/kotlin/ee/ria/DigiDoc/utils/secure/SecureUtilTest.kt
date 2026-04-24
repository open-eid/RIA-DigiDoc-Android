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
