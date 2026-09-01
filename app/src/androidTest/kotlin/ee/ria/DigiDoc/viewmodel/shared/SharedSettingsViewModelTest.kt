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

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.documentfile.provider.DocumentFile
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.DIR_TSA_CERT
import ee.ria.DigiDoc.common.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.libdigidocpp.DigiDocConf
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.TimeZone

@RunWith(MockitoJUnitRunner::class)
class SharedSettingsViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockWebServer = MockWebServer()

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var configurationRepository: ConfigurationRepository

    @Mock
    private lateinit var activityManager: ActivityManager

    companion object {
        private const val AWAIT_ERROR_TIMEOUT = 10_000L

        private lateinit var configurationLoader: ConfigurationLoader
        private lateinit var configurationRepository: ConfigurationRepository

        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    configurationLoader =
                        ConfigurationLoaderImpl(
                            Gson(),
                            CentralConfigurationRepositoryImpl(
                                CentralConfigurationServiceImpl(context, ConfigurationProperty()),
                            ),
                            ConfigurationProperty(),
                            ConfigurationPropertiesImpl(),
                            ConfigurationSignatureVerifierImpl(),
                        )
                    configurationRepository = ConfigurationRepositoryImpl(configurationLoader)
                    LibdigidocLibraryLoader().init(context)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context

    private lateinit var dataStore: DataStore

    private lateinit var initialization: Initialization

    private lateinit var viewModel: SharedSettingsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStore = DataStore(context)
        LibdigidocLibraryLoader().init(context)
        initialization = Initialization(configurationRepository)
        initialization.overrideProxy("", 80, "", "")
        dataStore.setProxySetting(ProxySetting.NO_PROXY)
        dataStore.setProxyHost("")
        dataStore.setProxyPort(80)
        dataStore.setProxyUsername("")
        dataStore.setProxyPassword("")
        viewModel =
            SharedSettingsViewModel(
                context = context,
                contentResolver = contentResolver,
                dataStore = dataStore,
                configurationRepository = configurationRepository,
                initialization = initialization,
                activityManager = activityManager,
            )
    }

    @Test
    fun sharedSettingsViewModel_init_success() {
        val result = viewModel.dataStore.getCountry()

        assertEquals(0, result)
    }

    @Test
    fun sharedSettingsViewModel_resetToDefaultSettings_success() {
        val file =
            AssetFile.getResourceFileAsFile(
                context,
                "siva.cer",
                ee.ria.DigiDoc.common.R.raw.siva,
            )
        val uri = Uri.fromFile(file)
        saveTsaCert(uri)
        viewModel.resetToDefaultSettings()

        // resetSigningSettings
        assertEquals(DEFAULT_UUID_VALUE, dataStore.getSettingsUUID())
        assertEquals("", dataStore.getSettingsTSAUrl())
        assertFalse(dataStore.getSettingsAskRoleAndAddress())
        assertFalse(dataStore.getSettingsDefaultLTA())
        assertFalse(dataStore.getIsTsaCertificateViewVisible())

        assertEquals("", dataStore.getTSACertName())

        // resetRightsSettings
        assertTrue(dataStore.getSettingsOpenAllFileTypes())
        assertFalse(dataStore.getSettingsAllowScreenshots())

        // resetSivaSettings
        assertEquals(SivaSetting.DEFAULT, dataStore.getSivaSetting())
        assertEquals("", dataStore.getSettingsSivaUrl())
        assertEquals("", dataStore.getSettingsSivaCertName())

        // resetProxySettings
        assertEquals(ProxySetting.NO_PROXY, dataStore.getProxySetting())
        assertEquals("", dataStore.getProxyHost())
        assertEquals(80, dataStore.getProxyPort())
        assertEquals("", dataStore.getProxyUsername())
        assertEquals("", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_resetToDefaultSettings_resetsDefaultLTAToFalse() {
        dataStore.setSettingsDefaultLTA(true)

        viewModel.resetToDefaultSettings()

        assertFalse(dataStore.getSettingsDefaultLTA())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_savesManualProxySettings() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(manualProxySettings)

        assertEquals("proxyHost", dataStore.getProxyHost())
        assertEquals(8080, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_keepsManualProxySettingsWhenSystemProxyIsChosen() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(manualProxySettings)

        System.setProperty("http.proxyHost", "systemProxyHost")
        dataStore.setProxySetting(ProxySetting.SYSTEM_PROXY)
        try {
            viewModel.saveProxySettings(ManualProxy("", 80, "", ""))
        } finally {
            System.clearProperty("http.proxyHost")
        }

        assertEquals("proxyHost", dataStore.getProxyHost())
        assertEquals(8080, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_keepsManualProxySettingsWhenNoProxyIsChosen() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(manualProxySettings)

        dataStore.setProxySetting(ProxySetting.NO_PROXY)
        viewModel.saveProxySettings(ManualProxy("", 80, "", ""))

        assertEquals("proxyHost", dataStore.getProxyHost())
        assertEquals(8080, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_clearsLibdigidocppProxyWhenNoProxyIsChosen() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        viewModel.saveProxySettings(ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass"))

        dataStore.setProxySetting(ProxySetting.NO_PROXY)
        viewModel.saveProxySettings(ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass"))

        assertEquals("", DigiDocConf.instance().proxyHost())
    }

    @Test
    fun sharedSettingsViewModel_updateData_successWithValidCertFile() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val file =
            AssetFile.getResourceFileAsFile(
                context,
                "siva.cer",
                ee.ria.DigiDoc.common.R.raw.siva,
            )
        val uri = Uri.fromFile(file)
        viewModel.handleSivaFile(uri)

        val validUrl = "https://valid-siva-url.com"
        viewModel.updateSivaData(validUrl, context)

        assertEquals(validUrl, viewModel.previousSivaUrl.value)
        assertNotNull(viewModel.sivaCertificate.value)
        assertEquals("*.eesti.ee", viewModel.sivaIssuedTo.value)
        assertEquals("30.09.2024 (Expired)", viewModel.sivaValidTo.value)
    }

    @Test(expected = Test.None::class)
    fun sharedSettingsViewModel_updateSivaData_withInvalidCertFile() {
        val file = createTempFileWithStringContent("invalid_cert", "invalid_cert")
        val uri = Uri.fromFile(file)
        viewModel.handleSivaFile(uri)

        val validUrl = "https://valid-siva-url.com"
        viewModel.updateSivaData(validUrl, context)

        assertEquals(validUrl, viewModel.previousSivaUrl.value)
    }

    @Test
    fun sharedSettingsViewModel_updateTsaData_successWithValidCertFile() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val file =
            AssetFile.getResourceFileAsFile(
                context,
                "siva.cer",
                ee.ria.DigiDoc.common.R.raw.siva,
            )
        val uri = Uri.fromFile(file)
        viewModel.handleTsaFile(uri)

        val validUrl = "https://valid-tsa-url.com"
        viewModel.updateTsaData(validUrl, context)

        assertEquals(validUrl, viewModel.previousTsaUrl.value)
        assertNotNull(viewModel.tsaCertificate.value)
        assertEquals("*.eesti.ee", viewModel.tsaIssuedTo.value)
        assertEquals("30.09.2024 (Expired)", viewModel.tsaValidTo.value)
    }

    @Test(expected = Test.None::class)
    fun sharedSettingsViewModel_updateTsaData_withInvalidCertFile() {
        val file = createTempFileWithStringContent("invalid_cert", "invalid_cert")
        val uri = Uri.fromFile(file)
        viewModel.handleTsaFile(uri)

        val validUrl = "https://valid-tsa-url.com"
        viewModel.updateTsaData(validUrl, context)

        assertEquals(validUrl, viewModel.previousTsaUrl.value)
    }

    @Test
    fun sharedSettingsViewModel_handleSivaFile_success() {
        val file =
            AssetFile.getResourceFileAsFile(
                context,
                "siva.cer",
                ee.ria.DigiDoc.common.R.raw.siva,
            )

        val uri = Uri.fromFile(file)
        viewModel.handleSivaFile(uri)
        assertEquals("sivaCert", dataStore.getSettingsSivaCertName())
    }

    @Test(expected = Test.None::class)
    fun sharedSettingsViewModel_handleFile_handleSivaError() {
        val uri: Uri = mock()

        viewModel.handleSivaFile(uri)
    }

    @Test
    fun sharedSettingsViewModel_handleTsaFile_success() {
        val file =
            AssetFile.getResourceFileAsFile(
                context,
                "siva.cer",
                ee.ria.DigiDoc.common.R.raw.siva,
            )

        val uri = Uri.fromFile(file)
        viewModel.handleTsaFile(uri)
        assertEquals("tsaCert", dataStore.getTSACertName())
    }

    @Test(expected = Test.None::class)
    fun sharedSettingsViewModel_handleFile_handleTsaError() {
        val uri: Uri = mock()

        viewModel.handleTsaFile(uri)
    }

    @Test
    fun sharedSettingsViewModel_checkConnection_savesManualProxySettings() {
        mockWebServer.enqueue(MockResponse().setResponseCode(407))
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)

        viewModel.checkConnection(localProxy("proxyUser", "proxyPass"))

        assertEquals("127.0.0.1", dataStore.getProxyHost())
        assertEquals(mockWebServer.port, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_checkConnection_reportsWrongCredentialsWhenProxyDemandsAuthentication() {
        mockWebServer.enqueue(MockResponse().setResponseCode(407))
        mockWebServer.enqueue(MockResponse().setResponseCode(407))
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)

        viewModel.checkConnection(localProxy("proxyUser", "wrongPass"))

        assertEquals(
            R.string.main_settings_proxy_check_username_and_password,
            awaitErrorMessage(),
        )
    }

    @Test
    fun sharedSettingsViewModel_checkConnection_reportsWrongCredentialsWhenProxyForbidsConnect() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)

        viewModel.checkConnection(localProxy("proxyUser", "proxyPass"))

        assertEquals(
            R.string.main_settings_proxy_check_username_and_password,
            awaitErrorMessage(),
        )
    }

    @Test
    fun sharedSettingsViewModel_checkConnection_reportsUnsuccessfulWhenProxyFailsForAnotherReason() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)

        viewModel.checkConnection(localProxy("proxyUser", "proxyPass"))

        assertEquals(
            R.string.main_settings_proxy_check_connection_unsuccessful,
            awaitErrorMessage(),
        )
    }

    private fun localProxy(
        username: String,
        password: String,
    ) = ManualProxy("127.0.0.1", mockWebServer.port, username, password)

    private fun awaitErrorMessage(): Int? {
        val deadline = System.currentTimeMillis() + AWAIT_ERROR_TIMEOUT
        while (System.currentTimeMillis() < deadline) {
            viewModel.errorState.value?.let { return it }
            Thread.sleep(50)
        }
        return null
    }

    @Test
    fun sharedSettingsViewModel_recreateActivity_successChangingRecreateActivityValue() {
        viewModel.recreateActivity()

        verify(activityManager).setShouldRecreateActivity(true)
    }

    @Suppress("SameParameterValue")
    private fun createTempFileWithStringContent(
        filename: String,
        content: String,
    ): File {
        val tempFile = File.createTempFile(filename, ".txt", context.cacheDir)
        Files.write(tempFile.toPath(), content.toByteArray(Charset.defaultCharset()))
        return tempFile
    }

    private fun saveTsaCert(uri: Uri) {
        try {
            val initialStream: InputStream? = contentResolver.openInputStream(uri)
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            if (documentFile != null) {
                val tsaCertFolder = File(context.filesDir, DIR_TSA_CERT)
                if (!tsaCertFolder.exists()) {
                    tsaCertFolder.mkdirs()
                }

                var fileName = documentFile.name
                if (fileName.isNullOrEmpty()) {
                    fileName = "tsaCert"
                }
                val tsaFile = File(tsaCertFolder, fileName)

                FileUtils.copyInputStreamToFile(initialStream, tsaFile)

                dataStore.setTSACertName(tsaFile.name)
            }
        } catch (_: Exception) {
            // Do nothing
        }
    }
}
