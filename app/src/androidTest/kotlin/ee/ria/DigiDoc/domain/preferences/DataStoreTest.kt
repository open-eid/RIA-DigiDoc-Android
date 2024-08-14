@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utils.Route
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.Locale

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
                    val configurationRepository = Mockito.mock(ConfigurationRepository::class.java)
                    Initialization(configurationRepository)
                        .init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        dataStore = DataStore(context)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.all.clear()
        preferences.all.forEach { (key, _) ->
            preferences.edit().remove(key).apply()
        }

        val encryptedPreferences: SharedPreferences = EncryptedPreferences.getEncryptedPreferences(context)

        encryptedPreferences.all?.clear()
        encryptedPreferences.all?.forEach { (key, _) ->
            encryptedPreferences.edit().remove(key).apply()
        }
    }

    @Test
    fun dataStore_getSignatureAddMethod_success() {
        val result = dataStore.getSignatureAddMethod()

        assertEquals(Route.MobileId.route, result)
    }

    @Test
    fun dataStore_setSignatureAddMethod_success() {
        dataStore.setSignatureAddMethod(Route.SmartId.route)

        val result = dataStore.getSignatureAddMethod()

        assertEquals(Route.SmartId.route, result)
    }

    @Test
    fun dataStore_getRoles_success() {
        val result = dataStore.getRoles()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setRoles_success() {
        dataStore.setRoles("role")

        val result = dataStore.getRoles()

        assertEquals("role", result)
    }

    @Test
    fun dataStore_getRoleCity_success() {
        val result = dataStore.getRoles()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setRoleCity_success() {
        dataStore.setRoleCity("Tallinn")

        val result = dataStore.getRoleCity()

        assertEquals("Tallinn", result)
    }

    @Test
    fun dataStore_getRoleState_success() {
        val result = dataStore.getRoleState()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setRoleState_success() {
        dataStore.setRoleState("Harju")

        val result = dataStore.getRoleState()

        assertEquals("Harju", result)
    }

    @Test
    fun dataStore_getRoleCountry_success() {
        val result = dataStore.getRoleCountry()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setRoleCountry_success() {
        dataStore.setRoleCountry("Estonia")

        val result = dataStore.getRoleCountry()

        assertEquals("Estonia", result)
    }

    @Test
    fun dataStore_getRoleZip_success() {
        val result = dataStore.getRoleCountry()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setRoleZip_success() {
        dataStore.setRoleZip("13666")

        val result = dataStore.getRoleZip()

        assertEquals("13666", result)
    }

    @Test
    fun dataStore_getPhoneNo_success() {
        val result = dataStore.getPhoneNo()

        assertEquals("372", result)
    }

    @Test
    fun dataStore_setPhoneNo_success() {
        dataStore.setPhoneNo("3725211765")

        val result = dataStore.getPhoneNo()

        assertEquals("3725211765", result)
    }

    @Test
    fun dataStore_getPersonalCode_success() {
        val result = dataStore.getPersonalCode()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setPersonalCode_success() {
        dataStore.setPersonalCode("45608303719")

        val result = dataStore.getPersonalCode()

        assertEquals("45608303719", result)
    }

    @Test
    fun dataStore_getSidPersonalCode_success() {
        val result = dataStore.getSidPersonalCode()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setSidPersonalCode_success() {
        dataStore.setSidPersonalCode("45608303719")

        val result = dataStore.getSidPersonalCode()

        assertEquals("45608303719", result)
    }

    @Test
    fun dataStore_getCountry_success() {
        val result = dataStore.getCountry()

        assertEquals(0, result)
    }

    @Test
    fun dataStore_setCountry_success() {
        dataStore.setCountry(1)

        val result = dataStore.getCountry()

        assertEquals(1, result)
    }

    @Test
    fun dataStore_getSettingsUUID_success() {
        val result = dataStore.getSettingsUUID()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setSettingsUUID_success() {
        dataStore.setSettingsUUID("0000-0000-0000-0000")

        val result = dataStore.getSettingsUUID()

        assertEquals("0000-0000-0000-0000", result)
    }

    @Test
    fun dataStore_getSettingsTSAUrl_success() {
        val result = dataStore.getSettingsTSAUrl()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setSettingsTSAUrl_success() {
        dataStore.setSettingsTSAUrl("https://www.sk.ee/tsa/")

        val result = dataStore.getSettingsTSAUrl()

        assertEquals("https://www.sk.ee/tsa/", result)
    }

    @Test
    fun dataStore_getSettingsAskRoleAndAddress_success() {
        val result = dataStore.getSettingsAskRoleAndAddress()

        assertEquals(false, result)
    }

    @Test
    fun dataStore_setSettingsAskRoleAndAddress_success() {
        dataStore.setSettingsAskRoleAndAddress(true)

        val result = dataStore.getSettingsAskRoleAndAddress()

        assertEquals(true, result)
    }

    @Test
    fun dataStore_getSettingsOpenAllFileTypes_success() {
        val result = dataStore.getSettingsOpenAllFileTypes()

        assertEquals(true, result)
    }

    @Test
    fun dataStore_setSettingsOpenAllFileTypes_success() {
        dataStore.setSettingsOpenAllFileTypes(false)

        val result = dataStore.getSettingsOpenAllFileTypes()

        assertEquals(false, result)
    }

    @Test
    fun dataStore_getSettingsAllowScreenshots_success() {
        val result = dataStore.getSettingsAllowScreenshots()

        assertEquals(false, result)
    }

    @Test
    fun dataStore_setSettingsAllowScreenshots_success() {
        dataStore.setSettingsAllowScreenshots(true)

        val result = dataStore.getSettingsAllowScreenshots()

        assertEquals(true, result)
    }

    @Test
    fun dataStore_getProxySetting_success() {
        val result = dataStore.getProxySetting()

        assertEquals(ProxySetting.NO_PROXY, result)
    }

    @Test
    fun dataStore_setProxySetting_success() {
        dataStore.setProxySetting(ProxySetting.SYSTEM_PROXY)

        val result = dataStore.getProxySetting()

        assertEquals(ProxySetting.SYSTEM_PROXY, result)
    }

    @Test
    fun dataStore_getProxyHost_success() {
        val result = dataStore.getProxyHost()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setProxyHost_success() {
        dataStore.setProxyHost("proxy.example.com")

        val result = dataStore.getProxyHost()

        assertEquals("proxy.example.com", result)
    }

    @Test
    fun dataStore_getProxyPort_success() {
        val result = dataStore.getProxyPort()

        assertEquals(80, result)
    }

    @Test
    fun dataStore_setProxyPort_success() {
        dataStore.setProxyPort(443)

        val result = dataStore.getProxyPort()

        assertEquals(443, result)
    }

    @Test
    fun dataStore_getProxyUsername_success() {
        val result = dataStore.getProxyUsername()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setProxyUsername_success() {
        dataStore.setProxyUsername("test")

        val result = dataStore.getProxyUsername()

        assertEquals("test", result)
    }

    @Test
    fun dataStore_getIsLogFileGenerationEnabled_success() {
        val result = dataStore.getIsLogFileGenerationEnabled()

        assertEquals(false, result)
    }

    @Test
    fun dataStore_setIsLogFileGenerationEnabled_success() {
        dataStore.setIsLogFileGenerationEnabled(true)

        val result = dataStore.getIsLogFileGenerationEnabled()

        assertEquals(true, result)
    }

    @Test
    fun dataStore_setIsLogFileGenerationRunning_success() {
        dataStore.setIsLogFileGenerationRunning(true)

        val result = dataStore.getIsLogFileGenerationRunning()

        assertEquals(true, result)
    }

    @Test
    fun dataStore_getSettingsSivaUrl_success() {
        val result = dataStore.getSettingsSivaUrl()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setSettingsSivaUrl_success() {
        dataStore.setSettingsSivaUrl("test")

        val result = dataStore.getSettingsSivaUrl()

        assertEquals("test", result)
    }

    @Test
    fun dataStore_getSettingsSivaCertName_success() {
        val result = dataStore.getSettingsSivaCertName()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setSettingsSivaCertName_success() {
        dataStore.setSettingsSivaCertName("test")

        val result = dataStore.getSettingsSivaCertName()

        assertEquals("test", result)
    }

    @Test
    fun dataStore_getLocale_success() {
        val result = dataStore.getLocale()

        assertEquals(null, result)
    }

    @Test
    fun dataStore_setLocale_success() {
        dataStore.setLocale(Locale.ENGLISH)

        val result = dataStore.getLocale()

        assertEquals(Locale.ENGLISH, result)
    }

    @Test
    fun dataStore_setLocale_returnNullIfSetNull() {
        dataStore.setLocale(null)

        val result = dataStore.getLocale()

        assertEquals(null, result)
    }

    @Test
    fun dataStore_getLibdigidocppVersion_success() {
        val result = dataStore.getLibdigidocppVersion()

        assertEquals("3.17.1.1420", result)
    }

    @Test
    fun dataStore_getProxyPassword_success() {
        val result = dataStore.getProxyPassword()

        assertEquals("", result)
    }

    @Test
    fun dataStore_setProxyPassword_success() {
        dataStore.setProxyPassword("testpwd")

        val result = dataStore.getProxyPassword()

        assertEquals("testpwd", result)
    }

    @Test
    fun dataStore_getManualProxySettings_success() {
        val result = dataStore.getManualProxySettings()

        assertEquals("", result.host)
        assertEquals(80, result.port)
        assertEquals("", result.username)
        assertEquals("", result.password)
    }
}
