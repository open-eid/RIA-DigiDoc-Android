@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.test.AssetFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class SharedContainerViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    private lateinit var viewModel: SharedContainerViewModel

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = SharedContainerViewModel(context, contentResolver)
    }

    @Test
    fun sharedContainerViewModel_saveContainerFile_success() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        intent.data = Uri.fromFile(file)
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveContainerFile(file, activityResult)
    }

    @Test
    fun sharedContainerViewModel_saveContainerFile_intentNull() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        intent.data = Uri.fromFile(file)
        val activityResult = ActivityResult(-1, null)
        viewModel.saveContainerFile(file, activityResult)
    }

    @Test
    fun sharedContainerViewModel_saveContainerFile_intentDataNull() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveContainerFile(file, activityResult)
    }

    @Test(expected = FileNotFoundException::class)
    fun sharedContainerViewModel_saveContainerFile_throwsFileNotFoundException() {
        val file = createTempFileWithStringContent("test", "Test content")
        val intent = Intent()
        intent.data = mock(Uri::class.java)
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveContainerFile(file, activityResult)
    }

    @Test(expected = NullPointerException::class)
    fun sharedContainerViewModel_saveContainerFile_throwsNullPointerException() {
        val file = mock(File::class.java)
        val intent = Intent()
        intent.data = mock(Uri::class.java)
        val activityResult = ActivityResult(-1, intent)
        viewModel.saveContainerFile(file, activityResult)
    }

    @Test
    fun sharedContainerViewModel_getContainerDataFile_success() {
        val file = createTempFileWithStringContent("test", "Test content")
        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, file, listOf(file))
            }

        val dataFile = signedContainer.getDataFiles().first()

        val result = viewModel.getContainerDataFile(signedContainer, dataFile)

        if (result != null) {
            assertEquals(file.name, result.name)
        }
    }

    @Test
    fun sharedContainerViewModel_getContainerDataFile_returnNull() {
        val file = createTempFileWithStringContent("test", "Test content")
        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, file, listOf(file))
            }

        val dataFile = signedContainer.getDataFiles().first()

        val result = viewModel.getContainerDataFile(null, dataFile)

        assertNull(result)
    }

    @Test
    fun sharedContainerViewModel_removeSignature_success() {
        val container = AssetFile.getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)

        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, container, listOf(container))
            }

        val signature = signedContainer.getSignatures().first()

        viewModel.removeSignature(signedContainer, signature)

        assertEquals(1, viewModel.signedContainer.value?.getSignatures()?.size)
    }

    @Test
    fun sharedContainerViewModel_removeSignature_returnNullIfDataFileNull() {
        val container = AssetFile.getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)

        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, container, listOf(container))
            }

        viewModel.removeSignature(signedContainer, null)

        assertNull(viewModel.signedContainer.value)
    }

    @Test
    fun sharedContainerViewModel_removeSignature_returnNullIfContainerNull() {
        val container = AssetFile.getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)

        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, container, listOf(container))
            }

        val signature = signedContainer.getSignatures().first()

        viewModel.removeSignature(null, signature)

        assertNull(viewModel.signedContainer.value)
    }

    fun sharedContainerViewModel_removeContainerDataFile_success() =
        runBlocking {
            val file = createTempFileWithStringContent("test", "Test content")
            val anotherFile = createTempFileWithStringContent("test2", "Another file")
            var signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file))
                }

            signedContainer =
                runBlocking {
                    signedContainer.addDataFiles(listOf(anotherFile))
                }

            val dataFile = signedContainer.getDataFiles().first()

            viewModel.removeContainerDataFile(signedContainer, dataFile)

            assertEquals(1, viewModel.signedContainer.value?.getDataFiles()?.size)
        }

    @Test(expected = ContainerDataFilesEmptyException::class)
    fun sharedContainerViewModel_removeContainerDataFile_throwsException() =
        runBlocking {
            val file = createTempFileWithStringContent("test", "Test content")
            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file))
                }

            val dataFile = signedContainer.getDataFiles().first()

            viewModel.removeContainerDataFile(signedContainer, dataFile)
        }

    @Test
    fun sharedContainerViewModel_removeContainerDataFile_returnNullIfDataFileNull() =
        runBlocking {
            val file = createTempFileWithStringContent("test", "Test content")
            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file))
                }

            viewModel.removeContainerDataFile(signedContainer, null)

            assertNull(viewModel.signedContainer.value)
        }

    @Test
    fun sharedContainerViewModel_removeContainerDataFile_returnNullIfContainerNull() =
        runBlocking {
            val file = createTempFileWithStringContent("test", "Test content")
            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file))
                }

            val dataFile = signedContainer.getDataFiles().first()

            viewModel.removeContainerDataFile(null, dataFile)

            assertNull(viewModel.signedContainer.value)
        }

    @Test
    fun sharedContainerViewModel_setSignedSidStatus_success() {
        viewModel.setSignedSidStatus(SessionStatusResponseProcessStatus.OK)

        assertEquals(SessionStatusResponseProcessStatus.OK, viewModel.signedSidStatus.value)
    }

    @Test
    fun sharedContainerViewModel_setSignedMidStatus_success() {
        viewModel.setSignedMidStatus(MobileCreateSignatureProcessStatus.OK)

        assertEquals(MobileCreateSignatureProcessStatus.OK, viewModel.signedMidStatus.value)
    }

    @Test
    fun sharedContainerViewModel_setSignedContainer_success() {
        val uri: Uri = mock()
        val uris = listOf(uri)
        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris)
            }

        viewModel.setSignedContainer(signedContainer)

        assertEquals(signedContainer, viewModel.signedContainer.value)
    }

    @Test
    fun sharedContainerViewModel_resetSignedContainer_success() {
        val uri: Uri = mock()
        val uris = listOf(uri)
        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris)
            }

        viewModel.setSignedContainer(signedContainer)

        viewModel.resetSignedContainer()

        assertEquals(null, viewModel.signedContainer.value)
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
