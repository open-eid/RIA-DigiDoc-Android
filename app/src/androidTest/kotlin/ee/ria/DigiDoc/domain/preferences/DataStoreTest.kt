@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utils.Route
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DataStoreTest {
    private lateinit var context: Context

    private lateinit var dataStore: DataStore

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    Initialization.init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        dataStore = DataStore(context)
    }

    @After
    fun tearDown() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.all.clear()
        preferences.all.forEach { (key, _) ->
            preferences.edit().remove(key).apply()
        }
    }

    @Test
    fun testGetSignatureAddMethod() {
        val result = dataStore.getSignatureAddMethod()

        assertEquals(Route.MobileId.route, result)
    }

    @Test
    fun testSetSignatureAddMethod() {
        dataStore.setSignatureAddMethod(Route.SmartId.route)

        val result = dataStore.getSignatureAddMethod()

        assertEquals(Route.SmartId.route, result)
    }

    @Test
    fun testGetPhoneNo() {
        val result = dataStore.getPhoneNo()

        assertEquals("372", result)
    }

    @Test
    fun testSetPhoneNo() {
        dataStore.setPhoneNo("3725211765")

        val result = dataStore.getPhoneNo()

        assertEquals("3725211765", result)
    }

    @Test
    fun testGetPersonalCode() {
        val result = dataStore.getPersonalCode()

        assertEquals("", result)
    }

    @Test
    fun testSetPersonalCode() {
        dataStore.setPersonalCode("45608303719")

        val result = dataStore.getPersonalCode()

        assertEquals("45608303719", result)
    }

    @Test
    fun testGetSidPersonalCode() {
        val result = dataStore.getSidPersonalCode()

        assertEquals("", result)
    }

    @Test
    fun testSetSidPersonalCode() {
        dataStore.setSidPersonalCode("45608303719")

        val result = dataStore.getSidPersonalCode()

        assertEquals("45608303719", result)
    }

    @Test
    fun testGetCountry() {
        val result = dataStore.getCountry()

        assertEquals(0, result)
    }

    @Test
    fun testSetCountry() {
        dataStore.setCountry(1)

        val result = dataStore.getCountry()

        assertEquals(1, result)
    }

    @Test
    fun testGetSettingsUUID() {
        val result = dataStore.getSettingsUUID()

        assertEquals("", result)
    }

    @Test
    fun testSetSettingsUUID() {
        dataStore.setSettingsUUID("0000-0000-0000-0000")

        val result = dataStore.getSettingsUUID()

        assertEquals("0000-0000-0000-0000", result)
    }

    @Test
    fun testGetSettingsTSAUrl() {
        val result = dataStore.getSettingsTSAUrl()

        assertEquals("", result)
    }

    @Test
    fun testSetSettingsTSAUrl() {
        dataStore.setSettingsTSAUrl("https://www.sk.ee/tsa/")

        val result = dataStore.getSettingsTSAUrl()

        assertEquals("https://www.sk.ee/tsa/", result)
    }

    @Test
    fun testGetSettingsAskRoleAndAddress() {
        val result = dataStore.getSettingsAskRoleAndAddress()

        assertEquals(false, result)
    }

    @Test
    fun testSetSettingsAskRoleAndAddress() {
        dataStore.setSettingsAskRoleAndAddress(true)

        val result = dataStore.getSettingsAskRoleAndAddress()

        assertEquals(true, result)
    }

    @Test
    fun testGetSettingsOpenAllFileTypes() {
        val result = dataStore.getSettingsOpenAllFileTypes()

        assertEquals(true, result)
    }

    @Test
    fun testSetSettingsOpenAllFileTypes() {
        dataStore.setSettingsOpenAllFileTypes(false)

        val result = dataStore.getSettingsOpenAllFileTypes()

        assertEquals(false, result)
    }

    @Test
    fun testGetSettingsAllowScreenshots() {
        val result = dataStore.getSettingsAllowScreenshots()

        assertEquals(false, result)
    }

    @Test
    fun testSetSettingsAllowScreenshots() {
        dataStore.setSettingsAllowScreenshots(true)

        val result = dataStore.getSettingsAllowScreenshots()

        assertEquals(true, result)
    }

    @Test
    fun testGetProxySetting() {
        val result = dataStore.getProxySetting()

        assertEquals(ProxySetting.NO_PROXY, result)
    }

    @Test
    fun testSetProxySetting() {
        dataStore.setProxySetting(ProxySetting.SYSTEM_PROXY)

        val result = dataStore.getProxySetting()

        assertEquals(ProxySetting.SYSTEM_PROXY, result)
    }

    @Test
    fun testGetProxyHost() {
        val result = dataStore.getProxyHost()

        assertEquals("", result)
    }

    @Test
    fun testSetProxyHost() {
        dataStore.setProxyHost("proxy.example.com")

        val result = dataStore.getProxyHost()

        assertEquals("proxy.example.com", result)
    }

    @Test
    fun testGetProxyPort() {
        val result = dataStore.getProxyPort()

        assertEquals(80, result)
    }

    @Test
    fun testSetProxyPort() {
        dataStore.setProxyPort(443)

        val result = dataStore.getProxyPort()

        assertEquals(443, result)
    }

    @Test
    fun testGetProxyUsername() {
        val result = dataStore.getProxyUsername()

        assertEquals("", result)
    }

    @Test
    fun testSetProxyUsername() {
        dataStore.setProxyUsername("test")

        val result = dataStore.getProxyUsername()

        assertEquals("test", result)
    }

    @Test
    fun testGetProxyPassword() {
        val result = dataStore.getProxyPassword(context)

        assertEquals("", result)
    }

    @Test
    fun testSetProxyPassword() {
        dataStore.setProxyPassword(context, "testpwd")

        val result = dataStore.getProxyPassword(context)

        assertEquals("testpwd", result)
    }

    @Test
    fun testGetManualProxySettings() {
        val result = dataStore.getManualProxySettings(context)

        assertEquals("", result.host)
        assertEquals(80, result.port)
        assertEquals("", result.username)
        assertEquals("", result.password)
    }
}
