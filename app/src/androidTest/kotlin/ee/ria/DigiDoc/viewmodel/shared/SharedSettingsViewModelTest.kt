@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.documentfile.provider.DocumentFile
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.Constant.DIR_TSA_CERT
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
import ee.ria.DigiDoc.utils.Constant
import kotlinx.coroutines.runBlocking
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

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var configurationRepository: ConfigurationRepository

    @Mock
    private lateinit var activityManager: ActivityManager

    companion object {
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
                                CentralConfigurationServiceImpl("Tests", ConfigurationProperty()),
                            ),
                            ConfigurationProperty(),
                            ConfigurationPropertiesImpl(),
                            ConfigurationSignatureVerifierImpl(),
                        )
                    configurationRepository = ConfigurationRepositoryImpl(context, configurationLoader)
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
        assertEquals(Constant.Defaults.DEFAULT_UUID_VALUE, dataStore.getSettingsUUID())
        assertEquals("", dataStore.getSettingsTSAUrl())
        assertFalse(dataStore.getSettingsAskRoleAndAddress())
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
    fun sharedSettingsViewModel_saveProxySettings_savesManualProxySettings() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(false, manualProxySettings)

        assertEquals("proxyHost", dataStore.getProxyHost())
        assertEquals(8080, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_savesSystemProxySettingsWithClearSettingsIsFalse() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(false, manualProxySettings)

        System.setProperty("http.proxyHost", "proxyHost")
        dataStore.setProxySetting(ProxySetting.SYSTEM_PROXY)
        viewModel.saveProxySettings(false, ManualProxy("", 0, "", ""))

        assertEquals("proxyHost", dataStore.getProxyHost())
        assertEquals(8080, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_savesNoProxySettingsWithClearSettingsIsFalse() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(false, manualProxySettings)

        dataStore.setProxySetting(ProxySetting.NO_PROXY)
        viewModel.saveProxySettings(false, ManualProxy("", 0, "", ""))

        assertEquals("proxyHost", dataStore.getProxyHost())
        assertEquals(8080, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_savesSystemProxySettingsWithClearSettingsIsTrue() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(false, manualProxySettings)
        System.setProperty("http.proxyHost", "")
        dataStore.setProxySetting(ProxySetting.SYSTEM_PROXY)
        viewModel.saveProxySettings(true, manualProxySettings)

        assertEquals("", dataStore.getProxyHost())
        assertEquals(80, dataStore.getProxyPort())
        assertEquals("", dataStore.getProxyUsername())
        assertEquals("", dataStore.getProxyPassword())
    }

    @Test
    fun sharedSettingsViewModel_saveProxySettings_savesNoProxySettingsWithClearSettingsIsTrue() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.saveProxySettings(false, manualProxySettings)

        dataStore.setProxySetting(ProxySetting.NO_PROXY)
        viewModel.saveProxySettings(true, manualProxySettings)

        assertEquals("", dataStore.getProxyHost())
        assertEquals(80, dataStore.getProxyPort())
        assertEquals("", dataStore.getProxyUsername())
        assertEquals("", dataStore.getProxyPassword())
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

    @Test(expected = Test.None::class)
    fun sharedSettingsViewModel_checkConnection_withInvalidManualProxySettings() {
        dataStore.setProxySetting(ProxySetting.MANUAL_PROXY)
        val manualProxySettings = ManualProxy("proxyHost", 8080, "proxyUser", "proxyPass")
        viewModel.checkConnection(manualProxySettings)

        assertEquals("proxyHost", dataStore.getProxyHost())
        assertEquals(8080, dataStore.getProxyPort())
        assertEquals("proxyUser", dataStore.getProxyUsername())
        assertEquals("proxyPass", dataStore.getProxyPassword())
    }

    @Test(expected = Test.None::class)
    fun sharedSettingsViewModel_checkConnection_withValidNoProxySettings() {
        dataStore.setProxySetting(ProxySetting.NO_PROXY)
        val manualProxySettings = ManualProxy("", 80, "", "")
        viewModel.saveProxySettings(true, manualProxySettings)
        viewModel.checkConnection(manualProxySettings)

        assertEquals("", dataStore.getProxyHost())
        assertEquals(80, dataStore.getProxyPort())
        assertEquals("", dataStore.getProxyUsername())
        assertEquals("", dataStore.getProxyPassword())
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
