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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class NetworkUtilTest {
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
}
