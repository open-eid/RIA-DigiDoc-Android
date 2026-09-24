// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
