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
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
            ldapCorpUrl = "https://www.example.com",
            midRestUrl = "https://www.example.com",
            midSkRestUrl = "https://www.example.com",
            sidV2RestUrl = "https://www.example.com",
            sidV2SkRestUrl = "https://www.example.com",
            certBundle = listOf("CERT0000111122224444"),
            configurationLastUpdateCheckDate = null,
            configurationUpdateDate = null,
        )

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
        // Prepare
        val calendar =
            Calendar.getInstance().apply {
                set(2023, Calendar.JANUARY, 1) // Year, Month, Day
            }
        val date = calendar.time
        val expectedFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val expected = expectedFormat.format(date)

        // Act
        val result = viewModel.getConfigurationDate(date)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun diagnosticsViewModel_createDiagnosticsFile_success() {
        val diagnosticsFileName =
            "ria_digidoc_" + viewModel.getAppVersion() + "_diagnostics.txt"
        val diagnosticsFilePath: String = (
            context.filesDir.path +
                File.separator + "diagnostics" + File.separator
        )
        val resultFile = viewModel.createDiagnosticsFile()

        assertEquals(diagnosticsFilePath + diagnosticsFileName, resultFile.path)
        assertTrue(resultFile.exists())
    }

    @Test
    fun diagnosticsViewModel_createLogFile_success() {
        val diagnosticsLogsFilePath =
            "ria_digidoc_" + viewModel.getAppVersion() + "_logs.txt"
        val logFolder = FileUtil.getLogsDirectory(context)
        val resultFile = viewModel.createLogFile()

        assertEquals(logFolder.path + File.separator + diagnosticsLogsFilePath, resultFile.path)
        assertTrue(resultFile.exists())
    }

    @Test
    fun diagnosticsViewModel_updateConfiguration_success() =
        runTest {
            viewModel.updateConfiguration(context)
            verify(configurationLoader).loadCentralConfiguration(context)
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

    private fun createTempFileWithStringContent(
        filename: String,
        content: String,
    ): File {
        val tempFile = File.createTempFile(filename, ".txt", context.cacheDir)
        Files.write(tempFile.toPath(), content.toByteArray(Charset.defaultCharset()))
        return tempFile
    }
}
