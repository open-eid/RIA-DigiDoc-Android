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
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.network.R
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.utils.NetworkUtil.constructClientBuilder
import okhttp3.Authenticator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.Proxy

class NetworkUtilTest {
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        resources = context.resources
        setProxyPreferences(ProxySetting.NO_PROXY, "")
    }

    @Test
    fun networkUtil_constructClientBuilder_contextNullReturnBuilder() {
        val result = constructClientBuilder(null)

        assertNotNull(result)
    }

    @Test
    fun networkUtil_constructClientBuilder_contextSuppliedReturnBuilder() {
        val result = constructClientBuilder(context)

        assertNotNull(result)
    }

    @Test
    fun networkUtil_constructClientBuilder_noProxyReturnBuilder() {
        preferences.edit {
            putString(
                resources.getString(R.string.main_settings_proxy_setting_key),
                ProxySetting.NO_PROXY.name,
            )
        }

        val result = constructClientBuilder(context)

        assertNotNull(result)
    }

    @Test
    fun networkUtil_constructClientBuilder_manualProxyReturnBuilder() {
        preferences.edit {
            putString(
                resources.getString(R.string.main_settings_proxy_setting_key),
                ProxySetting.MANUAL_PROXY.name,
            )
        }

        val result = constructClientBuilder(context)

        assertNotNull(result)
    }

    @Test
    fun networkUtil_constructClientBuilder_manualProxyWithoutHostInstallsNoAuthenticator() {
        setProxyPreferences(ProxySetting.MANUAL_PROXY, "proxyUser")
        preferences.edit {
            putString(resources.getString(R.string.main_settings_proxy_host_key), "")
        }

        val result = constructClientBuilder(context).build()

        assertEquals(Authenticator.NONE, result.proxyAuthenticator)
    }

    @Test
    fun networkUtil_constructClientBuilder_noProxyIgnoresStoredProxyCredentials() {
        setProxyPreferences(ProxySetting.NO_PROXY, "proxyUser")

        val result = constructClientBuilder(context).build()

        assertEquals(Proxy.NO_PROXY, result.proxy)
        assertEquals(Authenticator.NONE, result.proxyAuthenticator)
    }

    @Test
    fun networkUtil_constructClientBuilder_manualProxyAuthenticatesTheProxyOnly() {
        setProxyPreferences(ProxySetting.MANUAL_PROXY, "proxyUser")

        val result = constructClientBuilder(context).build()

        assertNotEquals(Authenticator.NONE, result.proxyAuthenticator)
        assertTrue(result.interceptors.isEmpty())
    }

    private fun setProxyPreferences(
        proxySetting: ProxySetting,
        username: String,
    ) {
        preferences.edit {
            putString(resources.getString(R.string.main_settings_proxy_setting_key), proxySetting.name)
            putString(resources.getString(R.string.main_settings_proxy_host_key), "proxyHost")
            putString(resources.getString(R.string.main_settings_proxy_username_key), username)
        }
    }
}
