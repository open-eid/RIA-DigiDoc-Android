@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
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
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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
    fun sharedContainerViewModel_getContainerDataFile_success() =
        runTest {
            val file = createTempFileWithStringContent("test", "Test content")
            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), true)
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
                SignedContainer.openOrCreate(context, file, listOf(file), true)
            }

        val dataFile =
            runBlocking {
                signedContainer.getDataFiles().first()
            }

        val result = viewModel.getContainerDataFile(null, dataFile)

        assertNull(result)
    }

    @Test
    fun sharedContainerViewModel_removeSignature_success() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container), true)
                }

            val signature = signedContainer.getSignatures().first()

            viewModel.removeSignature(signedContainer, signature)

            assertEquals(1, viewModel.signedContainer.value?.getSignatures()?.size)
        }

    @Test
    fun sharedContainerViewModel_removeSignature_returnSameSignaturesIfRemovingNullSignature() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container), true)
                }

            viewModel.removeSignature(signedContainer, null)

            assertEquals(signedContainer.getSignatures().size, viewModel.signedContainer.value?.getSignatures()?.size)
        }

    @Test
    fun sharedContainerViewModel_removeSignature_returnNullIfContainerNull() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container), true)
                }

            val signature = signedContainer.getSignatures().first()

            viewModel.removeSignature(null, signature)

            assertNull(viewModel.signedContainer.value)
        }

    @Test
    fun sharedContainerViewModel_removeContainerDataFile_success() =
        runBlocking {
            val file = createTempFileWithStringContent("test", "Test content")
            val anotherFile = createTempFileWithStringContent("test2", "Another file")
            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), true)
                }

            runBlocking {
                SignedContainer.addDataFiles(context, signedContainer, listOf(anotherFile))
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
                    SignedContainer.openOrCreate(context, file, listOf(file), true)
                }

            val dataFile = signedContainer.getDataFiles().first()

            viewModel.removeContainerDataFile(signedContainer, dataFile)
        }

    @Test
    fun sharedContainerViewModel_removeContainerDataFile_returnSameDataFilesIfRemovingNullDataFile() =
        runBlocking {
            val file = createTempFileWithStringContent("test", "Test content")
            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), true)
                }

            viewModel.removeContainerDataFile(signedContainer, null)

            assertEquals(signedContainer.getDataFiles().size, viewModel.signedContainer.value?.getDataFiles()?.size)
        }

    @Test
    fun sharedContainerViewModel_removeContainerDataFile_returnNullIfContainerNull() =
        runBlocking {
            val file = createTempFileWithStringContent("test", "Test content")
            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), true)
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
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        viewModel.setSignedContainer(signedContainer)

        assertEquals(signedContainer, viewModel.signedContainer.value)
    }

    @Test
    fun sharedContainerViewModel_setSignedIDCardStatus_successWithTrue() {
        viewModel.setSignedIDCardStatus(true)

        val signedIdCardStatus = viewModel.signedIDCardStatus.value

        if (signedIdCardStatus != null) {
            assertTrue(signedIdCardStatus)
        } else {
            fail("signedIDCardStatus is null")
        }
    }

    @Test
    fun sharedContainerViewModel_setSignedIDCardStatus_successWithFalse() {
        viewModel.setSignedIDCardStatus(false)

        val signedIdCardStatus = viewModel.signedIDCardStatus.value

        if (signedIdCardStatus != null) {
            assertFalse(signedIdCardStatus)
        } else {
            fail("signedIDCardStatus is null")
        }
    }

    @Test
    fun sharedContainerViewModel_removeLastContainer_success() {
        val uris = listOf(mock(Uri::class.java))

        runBlocking {
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)).thenReturn(
                SignedContainer(context),
            )
        }

        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        viewModel.setSignedContainer(signedContainer)

        viewModel.removeLastContainer()

        assertTrue(viewModel.nestedContainers.isEmpty())
    }

    @Test
    fun sharedContainerViewModel_removeLastContainer_noExceptionWithEmptyContainerList() {
        viewModel.removeLastContainer()

        assertTrue(viewModel.nestedContainers.isEmpty())
    }

    @Test
    fun sharedContainerViewModel_clearContainers_success() {
        val uris = listOf(mock(Uri::class.java))

        runBlocking {
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)).thenReturn(
                SignedContainer(context),
            )
        }

        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        viewModel.setSignedContainer(signedContainer)

        viewModel.clearContainers()

        assertTrue(viewModel.nestedContainers.isEmpty())
    }

    @Test
    fun sharedContainerViewModel_resetSignedContainer_success() {
        val uri: Uri = mock()
        val uris = listOf(uri)
        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        viewModel.setSignedContainer(signedContainer)

        viewModel.resetSignedContainer()

        assertNull(viewModel.signedContainer.value)
    }

    @Test
    fun sharedContainerViewModel_currentContainer_success() {
        val uris = listOf(mock(Uri::class.java))
        val nestedUris = listOf(mock(Uri::class.java))

        runBlocking {
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)).thenReturn(
                SignedContainer(context),
            )
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, nestedUris, true)).thenReturn(
                SignedContainer(context),
            )
        }

        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        val nestedSignedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, nestedUris, true)
            }

        viewModel.setSignedContainer(signedContainer)
        viewModel.setSignedContainer(nestedSignedContainer)

        assertEquals(nestedSignedContainer, viewModel.currentContainer())
    }

    @Test
    fun sharedContainerViewModel_currentContainer_returnNullWhenContainerListEmpty() {
        val uris = listOf<Uri>()
        val nestedUris = listOf<Uri>()

        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        val nestedSignedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, nestedUris, true)
            }

        viewModel.setSignedContainer(signedContainer)

        assertNull(viewModel.currentContainer())
    }

    @Test
    fun sharedContainerViewModel_isNestedContainer_returnTrue() {
        val uris = listOf(mock(Uri::class.java))
        val nestedUris = listOf(mock(Uri::class.java))

        runBlocking {
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)).thenReturn(
                SignedContainer(context),
            )
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, nestedUris, true)).thenReturn(
                SignedContainer(context),
            )
        }

        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        val nestedSignedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, nestedUris, true)
            }

        viewModel.setSignedContainer(signedContainer)
        viewModel.setSignedContainer(nestedSignedContainer)

        assertTrue(viewModel.isNestedContainer(nestedSignedContainer))
    }

    @Test
    fun sharedContainerViewModel_isNestedContainer_returnFalse() {
        val uris = listOf(mock(Uri::class.java))
        val nestedUris = listOf(mock(Uri::class.java))

        runBlocking {
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)).thenReturn(
                SignedContainer(context),
            )
            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, nestedUris, true)).thenReturn(
                SignedContainer(context),
            )
        }

        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
            }

        runBlocking {
            fileOpeningRepository.openOrCreateContainer(context, contentResolver, nestedUris, true)
        }

        viewModel.setSignedContainer(signedContainer)

        assertFalse(viewModel.isNestedContainer(signedContainer))
    }

    @Test
    fun sharedContainerViewModel_setExternalFileUri_success() =
        runTest {
            val uris = listOf(Uri.parse("content://example/uri"))

            viewModel.setExternalFileUris(uris)

            viewModel.externalFileUris.take(1).collect { collectedUri ->
                assertEquals(collectedUri, uris)
            }
        }

    @Test
    fun sharedContainerViewModel_resetExternalFileUri_success() =
        runTest {
            val uris = listOf(Uri.parse("content://example/uri"))
            viewModel.setExternalFileUris(uris)

            viewModel.resetExternalFileUris()

            viewModel.externalFileUris.take(1).collect { collectedUri ->
                assertEquals(listOf<Uri>(), collectedUri)
            }
        }

    @Test
    fun sharedContainerViewModel_setIsSivaConfirmed_success() {
        viewModel.setIsSivaConfirmed(false)

        assertTrue(viewModel.isSivaConfirmed.value == false)
    }

    @Test
    fun sharedContainerViewModel_resetIsSivaConfirmed_success() {
        viewModel.resetIsSivaConfirmed()

        assertTrue(viewModel.isSivaConfirmed.value == true)
    }

    @Test
    fun sharedContainerViewModel_setAddedFilesCount_success() {
        viewModel.setAddedFilesCount(1)

        assertTrue(viewModel.addedFilesCount.value == 1)
    }

    @Test
    fun sharedContainerViewModel_resetAddedFilesCount_success() {
        viewModel.resetAddedFilesCount()

        assertTrue(viewModel.addedFilesCount.value == 0)
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
