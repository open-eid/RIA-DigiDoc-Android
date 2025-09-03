@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class DiagnosticsViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var configurationLoader: ConfigurationLoader

    @Mock
    lateinit var configurationRepository: ConfigurationRepository

    private val configurationProvider =
        ConfigurationProvider(
            metaInf =
                ConfigurationProvider.MetaInf(
                    url = "https://www.example.com",
                    date = "2021-09-01",
                    serial = 1,
                    version = 1,
                ),
            sivaUrl = "https://www.example.com",
            tslUrl = "https://www.example.com",
            tslCerts = listOf("CERT0000111122224444"),
            tsaUrl = "https://www.example.com",
            ocspUrls = mapOf("issuer" to "https://www.example.com"),
            ldapPersonUrl = "https://www.example.com",
            ldapPersonUrls = listOf("https://www.example.com"),
            ldapCorpUrl = "https://www.example.com",
            midRestUrl = "https://www.example.com",
            midSkRestUrl = "https://www.example.com",
            sidV2RestUrl = "https://www.example.com",
            sidV2SkRestUrl = "https://www.example.com",
            certBundle = listOf("CERT0000111122224444"),
            configurationLastUpdateCheckDate = null,
            configurationUpdateDate = null,
            cdoc2Conf =
                mapOf(
                    "00000000-0000-0000-0000-000000000000" to
                        ConfigurationProvider.CDOC2Conf(
                            name = "RIA",
                            post = "https://cdoc2.id.ee:8443",
                            fetch = "https://cdoc2.id.ee:8444",
                        ),
                ),
            cdoc2UseKeyServer = false,
            cdoc2DefaultKeyServer = "00000000-0000-0000-0000-000000000000",
        )

    private lateinit var proxySetting: ProxySetting
    private lateinit var manualProxy: ManualProxy

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
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context

    private lateinit var dataStore: DataStore

    private lateinit var viewModel: DiagnosticsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStore = DataStore(context)
        viewModel =
            DiagnosticsViewModel(
                context,
                dataStore,
                configurationLoader,
                configurationRepository,
                contentResolver,
            )
        proxySetting = ProxySetting.NO_PROXY
        manualProxy = ManualProxy("", 80, "", "")
    }

    @Test
    fun diagnosticsViewModel_getSivaUrl_returnsNonEmptyDataStoreValue() {
        dataStore.setSettingsSivaUrl("https://example.com/siva")
        val result = viewModel.getSivaUrl()
        assertEquals("https://example.com/siva", result)
    }

    @Test
    fun diagnosticsViewModel_getSivaUrl_returnsUpdatedConfigurationValueWhenDataStoreEmpty() {
        val expectedUrl = "https://www.example.com"
        dataStore.setSettingsSivaUrl("")
        viewModel.updatedConfiguration = MutableLiveData(configurationProvider)

        val result = viewModel.getSivaUrl()

        assertEquals(expectedUrl, result)
    }

    @Test
    fun diagnosticsViewModel_getSivaUrl_returnsEmptyValueWhenDataStoreEmptyAndUpdatedConfigurationIsNull() {
        dataStore.setSettingsSivaUrl("")
        viewModel.updatedConfiguration = MutableLiveData(null)

        val result = viewModel.getSivaUrl()

        assertEquals("", result)
    }

    @Test
    fun diagnosticsViewModel_getTsaUrl_returnsNonEmptyDataStoreValue() {
        dataStore.setSettingsTSAUrl("https://example.com/tsa")
        val result = viewModel.getTsaUrl()
        assertEquals("https://example.com/tsa", result)
    }

    @Test
    fun diagnosticsViewModel_getTsaUrl_returnsUpdatedConfigurationValueWhenDataStoreIsEmpty() {
        val expectedUrl = "https://www.example.com"
        dataStore.setSettingsTSAUrl("")
        viewModel.updatedConfiguration = MutableLiveData(configurationProvider)

        val result = viewModel.getTsaUrl()

        assertEquals(expectedUrl, result)
    }

    @Test
    fun diagnosticsViewModel_getTsaUrl_returnsEmptyValueWhenDataStoreIsEmptyAndUpdatedConfigurationIsNull() {
        dataStore.setSettingsTSAUrl("")
        viewModel.updatedConfiguration = MutableLiveData(null)

        val result = viewModel.getTsaUrl()
        assertEquals("", result)
    }

    @Test
    fun diagnosticsViewModel_getDataStore_success() {
        val actualDataStore = viewModel.dataStore

        assertEquals(dataStore, actualDataStore)
    }

    @Test
    fun diagnosticsViewModel_getUpdatedConfiguration_success() {
        viewModel.updatedConfiguration = MutableLiveData(null)
        val actualUpdatedConfiguration = viewModel.updatedConfiguration

        assertNull(actualUpdatedConfiguration.value)
    }

    @Test
    fun diagnosticsViewModel_getRpUuid_returnsDefaultStringForEmptyDataStoreValue() {
        dataStore.setSettingsUUID("")
        val result = viewModel.getRpUuid()
        assertEquals(R.string.main_diagnostics_rpuuid_default, result)
    }

    @Test
    fun diagnosticsViewModel_getRpUuid_returnsCustomStringForNonEmptyDataStoreValue() {
        dataStore.setSettingsUUID("0000-0000-0000-0000-0000")
        val result = viewModel.getRpUuid()
        assertEquals(R.string.main_diagnostics_rpuuid_custom, result)
    }

    @Test
    fun diagnosticsViewModel_getConfigurationDate_returnsEmptyStringForNullDate() {
        val result = viewModel.getConfigurationDate(null)
        assertEquals("", result)
    }

    @Test
    fun diagnosticsViewModel_getConfigurationDate_returnsFormattedStringForNonNullDate() {
        val calendar =
            Calendar.getInstance().apply {
                set(2023, Calendar.JANUARY, 1) // Year, Month, Day
            }
        val date = calendar.time
        val expectedFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val expected = expectedFormat.format(date)

        val result = viewModel.getConfigurationDate(date)

        assertEquals(expected, result)
    }

    @Test
    fun diagnosticsViewModel_createDiagnosticsFile_success() {
        val diagnosticsFileName =
            "ria_digidoc_${viewModel.getAppVersion()}.${viewModel.getAppVersionCode()}_diagnostics.log"
        val diagnosticsFilePath: String = File(context.filesDir.path, "diagnostics").path
        val resultFile = viewModel.createDiagnosticsFile(context)

        assertEquals(File(diagnosticsFilePath, diagnosticsFileName).path, resultFile.path)
        assertTrue(resultFile.exists())
    }

    @Test
    fun diagnosticsViewModel_createLogFile_success() {
        val diagnosticsLogsFilePath =
            "ria_digidoc_${viewModel.getAppVersion()}.${viewModel.getAppVersionCode()}_logs.log"
        val logFolder = FileUtil.getLogsDirectory(context)
        val resultFile = viewModel.createLogFile(context)

        assertEquals(File(logFolder.path, diagnosticsLogsFilePath).path, resultFile.path)
        assertTrue(resultFile.exists())

        viewModel.resetLogs(context)

        // Verify that the logs directory is cleared
        val logDirectory = FileUtil.getLogsDirectory(context)
        assertTrue(logDirectory.exists())
        assertTrue(logDirectory.listFiles()?.isEmpty() ?: true)
    }

    @Test(expected = FileNotFoundException::class)
    fun diagnosticsViewModel_createLogFile_exception() {
        viewModel.resetLogs(context)
        viewModel.createLogFile(context)
    }

    @Test
    fun diagnosticsViewModel_updateConfiguration_success() =
        runTest {
            viewModel.updateConfiguration(context)
            verify(configurationLoader).loadCentralConfiguration(context, proxySetting, manualProxy)
        }

    @Test
    fun diagnosticsViewModel_saveFile_success() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        intent.data = Uri.fromFile(file)
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveFile(file, activityResult)
    }

    @Test
    fun diagnosticsViewModel_saveFile_intentNull() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        intent.data = Uri.fromFile(file)
        val activityResult = ActivityResult(-1, null)
        viewModel.saveFile(file, activityResult)
    }

    @Test
    fun diagnosticsViewModel_saveFile_intentDataNull() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveFile(file, activityResult)
    }

    @Test(expected = FileNotFoundException::class)
    fun diagnosticsViewModel_saveFile_throwsFileNotFoundException() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        intent.data = mock(Uri::class.java)
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveFile(file, activityResult)
    }

    @Test(expected = NullPointerException::class)
    fun diagnosticsViewModel_saveFile_throwsNullPointerException() {
        val file = mock(File::class.java)
        val intent = Intent()
        intent.data = mock(Uri::class.java)
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveFile(file, activityResult)
    }

    @Test
    fun diagnosticsViewModel_isCdoc2Selected_returnTrue() {
        dataStore.setCdocSetting(CDOCSetting.CDOC2)
        viewModel.updatedConfiguration = MutableLiveData(configurationProvider)

        val isCdoc2Selected = viewModel.isCdoc2Selected()

        assertTrue(isCdoc2Selected)
    }

    @Test
    fun diagnosticsViewModel_isCdoc2Selected_returnFalse() {
        dataStore.setCdocSetting(CDOCSetting.CDOC1)
        viewModel.updatedConfiguration = MutableLiveData(configurationProvider)

        val isCdoc2Selected = viewModel.isCdoc2Selected()

        assertFalse(isCdoc2Selected)
    }

    @Test
    fun diagnosticsViewModel_isCdoc2KeyServerUsed_returnTrue() {
        dataStore.setCdocSetting(CDOCSetting.CDOC2)
        dataStore.setUseOnlineEncryption(true)
        viewModel.updatedConfiguration = MutableLiveData(configurationProvider)

        val isCdoc2KeyServerUsed = viewModel.isCdoc2KeyServerUsed()

        assertTrue(isCdoc2KeyServerUsed)
    }

    @Test
    fun diagnosticsViewModel_isCdoc2KeyServerUsed_returnFalse() {
        dataStore.setCdocSetting(CDOCSetting.CDOC2)
        dataStore.setUseOnlineEncryption(false)
        viewModel.updatedConfiguration = MutableLiveData(configurationProvider)

        val isCdoc2KeyServerUsed = viewModel.isCdoc2KeyServerUsed()

        assertFalse(isCdoc2KeyServerUsed)
    }

    @Test
    fun diagnosticsViewModel_getCdoc2KeyServerUUID_returnCorrectUUID() {
        dataStore.setCdocSetting(CDOCSetting.CDOC2)
        dataStore.setCDOC2UUID(DEFAULT_UUID_VALUE)
        viewModel.updatedConfiguration = MutableLiveData(configurationProvider)

        val cdoc2KeyServerUUID = viewModel.getCdoc2KeyServerUUID()

        assertEquals(DEFAULT_UUID_VALUE, cdoc2KeyServerUUID)
    }

    private fun createTempFileWithStringContent(
        filename: String,
        content: String,
    ): File {
        val tempFile = File.createTempFile(filename, ".txt", context.cacheDir)
        Files.write(tempFile.toPath(), content.toByteArray(Charset.defaultCharset()))
        return tempFile
    }
}
