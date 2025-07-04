@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.utils.ProxyUtil.getProxy
import ee.ria.DigiDoc.network.utils.ProxyUtil.getProxyValues
import junit.framework.TestCase.assertEquals
import okhttp3.Authenticator
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProxyUtilTest {
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
    fun proxyUtil_getProxyValues_proxySettingNoProxyReturnNull() {
        val result = getProxyValues(ProxySetting.NO_PROXY, null)

        assertNull(result)
    }

    @Test
    fun proxyUtil_getProxyValues_proxySettingNullReturnNull() {
        val result = getProxyValues(null, null)

        assertNull(result)
    }

    @Test
    fun proxyUtil_getProxyValues_proxySettingManualProxyDefaultReturnDefaultManualProxy() {
        val result = getProxyValues(ProxySetting.MANUAL_PROXY, null)

        assertNotNull(result)
        assertEquals("", result?.host)
        assertEquals(80, result?.port)
        assertEquals("", result?.username)
        assertEquals("", result?.password)
    }

    @Test
    fun proxyUtil_getProxyValues_proxySettingManualProxyReturnManualProxy() {
        val result =
            getProxyValues(
                ProxySetting.MANUAL_PROXY,
                ManualProxy("testhost", 88, "testuser", "testpwd"),
            )

        assertNotNull(result)
        assertEquals("testhost", result?.host)
        assertEquals(88, result?.port)
        assertEquals("testuser", result?.username)
        assertEquals("testpwd", result?.password)
    }

    @Test
    fun proxyUtil_getProxyValues_proxySettingSystemProxyDefaultValues() {
        System.clearProperty("http.proxyHost")
        System.clearProperty("http.proxyPort")
        System.clearProperty("http.proxyUser")
        System.clearProperty("http.proxyPassword")

        val result = getProxyValues(ProxySetting.SYSTEM_PROXY, null)

        assertNotNull(result)

        assertEquals("", result?.host)
        assertEquals(80, result?.port)
        assertEquals("", result?.username)
        assertEquals("", result?.password)
    }

    @Test
    fun proxyUtil_getProxyValues_proxySettingSystemProxyDefinedValues() {
        System.setProperty("http.proxyHost", "systemhost")
        System.setProperty("http.proxyPort", "84")
        System.setProperty("http.proxyUser", "systemuser")
        System.setProperty("http.proxyPassword", "systempwd")
        val result = getProxyValues(ProxySetting.SYSTEM_PROXY, null)

        assertNotNull(result)

        assertEquals("systemhost", result?.host)
        assertEquals(84, result?.port)
        assertEquals("systemuser", result?.username)
        assertEquals("systempwd", result?.password)
    }

    @Test
    fun proxyUtil_getProxy_proxySettingNoProxyManualProxySettingsNull() {
        val result = getProxy(ProxySetting.NO_PROXY, null)

        assertNull(result.proxy())
        assertEquals(Authenticator.Companion.NONE, result.authenticator())
        assertNull(result.manualProxy())
    }

    @Test
    fun proxyUtil_getProxy_proxySettingManualProxyManualProxySettingsNull() {
        val result = getProxy(ProxySetting.MANUAL_PROXY, null)

        assertNull(result.proxy())
        assertNull(result.manualProxy())
        assertNotEquals(Authenticator.Companion.NONE, result.authenticator())
    }

    @Test
    fun proxyUtil_getProxy_proxySettingSystemProxyDefaultManualProxySettingsNull() {
        System.clearProperty("http.proxyHost")
        System.clearProperty("http.proxyPort")
        System.clearProperty("http.proxyUser")
        System.clearProperty("http.proxyPassword")

        val result = getProxy(ProxySetting.SYSTEM_PROXY, null)

        assertNull(result.proxy())
        assertEquals(Authenticator.Companion.NONE, result.authenticator())
        assertNull(result.manualProxy())
    }

    @Test
    fun proxyUtil_getProxy_proxySettingSystemProxyDefinedManualProxySettingsNull() {
        System.setProperty("http.proxyHost", "systemhost")
        System.setProperty("http.proxyPort", "84")
        System.setProperty("http.proxyUser", "systemuser")
        System.setProperty("http.proxyPassword", "systempwd")

        val result = getProxy(ProxySetting.SYSTEM_PROXY, null)

        assertNotNull(result.proxy())

        assertNotEquals(Authenticator.Companion.NONE, result.authenticator())

        assertEquals("systemhost", result.manualProxy()?.host)
        assertEquals(84, result.manualProxy()?.port)
        assertEquals("systemuser", result.manualProxy()?.username)
        assertEquals("systempwd", result.manualProxy()?.password)
    }
}
