@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.testfiles.file.TestFileUtil.Companion.createZipWithTextFile
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
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verifyNoInteractions
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class FileOpeningViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    @Mock
    lateinit var fileChooserLauncher: ActivityResultLauncher<String>

    @Mock
    lateinit var signedContainerObserver: Observer<SignedContainer?>

    @Mock
    lateinit var filesAddedObserver: Observer<List<File>?>

    @Mock
    lateinit var errorStateObserver: Observer<Int?>

    @Mock
    lateinit var launchFilePickerObserver: Observer<Boolean?>

    @Mock
    lateinit var sivaRepository: SivaRepository

    private lateinit var viewModel: FileOpeningViewModel

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
        viewModel = FileOpeningViewModel(context, contentResolver, fileOpeningRepository, sivaRepository)
        viewModel.signedContainer.observeForever(signedContainerObserver)
        viewModel.errorState.observeForever(errorStateObserver)
        viewModel.launchFilePicker.observeForever(launchFilePickerObserver)
        viewModel.filesAdded.observeForever(filesAddedObserver)
    }

    @Test
    fun fileOpeningViewModel_resetContainer_success() =
        runTest {
            val file = createTempFileWithStringContent("test", content = "Test content")
            runBlocking {
                SignedContainer.openOrCreate(context, file, listOf(file), true)
            }

            viewModel.resetContainer()

            assertNull(viewModel.signedContainer.value)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun fileOpeningViewModel_resetFilesAdded_success() =
        runTest {
            viewModel.resetFilesAdded()

            verify(filesAddedObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_success() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", content = "Test content")
            val anotherFile = createTempFileWithStringContent("test2", content = "Another file")

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), true)
                }

            runBlocking {
                SignedContainer.addDataFiles(context, signedContainer, listOf(anotherFile))
            }

            val uriToFile = fileOpeningRepository.uriToFile(context, contentResolver, uri)

            `when`(
                fileOpeningRepository.isFileSizeValid(uriToFile),
            )
                .thenReturn(true)

            `when`(
                fileOpeningRepository.isFileAlreadyInContainer(uriToFile, signedContainer),
            )
                .thenReturn(false)

            `when`(
                fileOpeningRepository.getValidFiles(any(), eq(signedContainer)),
            )
                .thenReturn(listOf(file))

            viewModel.handleFiles(uris, signedContainer, true)

            verify(filesAddedObserver, atLeastOnce()).onChanged(listOf(file))
            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_throwException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", content = "Test content")
            val uriToFile = fileOpeningRepository.uriToFile(context, contentResolver, uri)

            val isSivaConfirmed = true

            val existingSignedContainer = SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)

            val exception = Exception("Could not add files to container")

            `when`(
                fileOpeningRepository.isFileSizeValid(uriToFile),
            )
                .thenReturn(true)

            `when`(
                fileOpeningRepository.addFilesToContainer(
                    eq(context),
                    any<SignedContainer>(),
                    any(),
                ),
            )
                .thenThrow(exception)
            viewModel.handleFiles(uris, existingSignedContainer, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(existingSignedContainer)
            verify(errorStateObserver, atLeastOnce()).onChanged(R.string.container_open_file_error)
        }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_throwEmptyFileException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", content = "Test content")

            val isSivaConfirmed = true

            val existingSignedContainer = SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)

            val uriToFile = fileOpeningRepository.uriToFile(context, contentResolver, uri)

            `when`(
                fileOpeningRepository.isFileSizeValid(uriToFile),
            )
                .thenReturn(false)

            viewModel.handleFiles(uris, existingSignedContainer, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(existingSignedContainer)
            verify(errorStateObserver, atLeastOnce()).onChanged(R.string.empty_file_error)
        }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_throwFileAlreadyExistsException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", content = "Test content")

            val isSivaConfirmed = true

            val uriToFile = fileOpeningRepository.uriToFile(context, contentResolver, uri)

            val existingSignedContainer = SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)

            `when`(
                fileOpeningRepository.isFileSizeValid(uriToFile),
            )
                .thenReturn(true)

            `when`(
                fileOpeningRepository.isFileAlreadyInContainer(uriToFile, existingSignedContainer),
            )
                .thenReturn(true)

            viewModel.handleFiles(uris, existingSignedContainer, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(existingSignedContainer)
            verify(errorStateObserver, atLeastOnce()).onChanged(R.string.signature_update_documents_add_error_exists)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_success() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", content = "Test content")

            val isSivaConfirmed = true

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)
                }

            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    context,
                    contentResolver,
                    uris,
                    isSivaConfirmed,
                ),
            )
                .thenReturn(signedContainer)

            `when`(sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed)).thenReturn(false)

            viewModel.handleFiles(uris, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_successWithMultipleURIs() =
        runTest {
            val uri1: Uri = mock()
            val uri2: Uri = mock()
            val uris = listOf(uri1, uri2)
            val file = createTempFileWithStringContent("test", content = "Test content")
            val isSivaConfirmed = true

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)
                }

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, isSivaConfirmed))
                .thenReturn(signedContainer)

            `when`(sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed)).thenReturn(false)

            viewModel.handleFiles(uris, null, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_successWithXades() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createZipWithTextFile("application/zip", "signatures.xml")

            val isSivaConfirmed = true

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)
                }

            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    context,
                    contentResolver,
                    uris,
                    isSivaConfirmed,
                ),
            )
                .thenReturn(signedContainer)

            `when`(sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed)).thenReturn(false)

            viewModel.handleFiles(uris, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_successWithCades() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createZipWithTextFile("application/zip", "signatures001.p7s")

            val isSivaConfirmed = true

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)
                }

            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    context,
                    contentResolver,
                    uris,
                    isSivaConfirmed,
                ),
            )
                .thenReturn(signedContainer)

            `when`(sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed)).thenReturn(false)

            viewModel.handleFiles(uris, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_showFileChooser_success() =
        runTest {
            viewModel.showFileChooser(fileChooserLauncher)

            verify(fileOpeningRepository).showFileChooser(fileChooserLauncher, "*/*")

            verify(launchFilePickerObserver, atLeastOnce()).onChanged(false)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failure() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val exception = EmptyFileException()

            val isSivaConfirmed = true

            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    context,
                    contentResolver,
                    uris,
                    isSivaConfirmed,
                ),
            )
                .thenThrow(exception)

            viewModel.handleFiles(uris, null, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver, atLeastOnce()).onChanged(R.string.empty_file_error)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_noSuchElementException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val exception = NoSuchElementException("No such element")

            val isSivaConfirmed = true

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, isSivaConfirmed))
                .thenThrow(exception)

            viewModel.handleFiles(uris, null, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(anyInt())
        }

    @Test
    fun fileOpeningViewModel_handleFiles_noInternetConnectionException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val exception = NoInternetConnectionException()

            val isSivaConfirmed = true

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, isSivaConfirmed))
                .thenThrow(exception)

            viewModel.handleFiles(uris, null, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(R.string.no_internet_connection)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failWhenNullContainer() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)

            val isSivaConfirmed = true

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, isSivaConfirmed))
                .thenReturn(null)

            viewModel.handleFiles(uris, null, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failWhenExceptionThrown() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val exception = Exception("Could not load selected files")

            val isSivaConfirmed = true

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, isSivaConfirmed))
                .thenThrow(exception)

            viewModel.handleFiles(uris, null, isSivaConfirmed = isSivaConfirmed)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(R.string.container_open_file_error)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failWithEmptyFileList() =
        runTest {
            val uris = emptyList<Uri>()

            viewModel.handleFiles(uris, null, isSivaConfirmed = true)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(null)
        }

    @Test
    fun fileOpeningViewModel_isSivaConfirmationNeeded_returnFalseWithEmptyFileList() =
        runTest {
            val uris = emptyList<Uri>()

            val isSivaConfirmationNeeded = viewModel.isSivaConfirmationNeeded(uris)

            assertFalse(isSivaConfirmationNeeded)
        }

    @Test
    fun fileOpeningViewModel_isSivaConfirmationNeeded_returnFalseWithMultipleFiles() =
        runTest {
            val uris = listOf(mock(Uri::class.java), mock(Uri::class.java))

            val isSivaConfirmationNeeded = viewModel.isSivaConfirmationNeeded(uris)

            assertFalse(isSivaConfirmationNeeded)
        }

    @Test
    fun fileOpeningViewModel_isSivaConfirmationNeeded_success() =
        runTest {
            val file = mock(File::class.java)
            val uris = listOf(Uri.fromFile(file))

            `when`(fileOpeningRepository.uriToFile(context, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningRepository.isSivaConfirmationNeeded(context, listOf(file))).thenReturn(true)

            val isSivaConfirmationNeeded = viewModel.isSivaConfirmationNeeded(uris)

            assertTrue(isSivaConfirmationNeeded)
        }

    @Test
    fun fileOpeningViewModel_isSivaConfirmationNeeded_returnTrue() =
        runTest {
            val file = createTempFileWithStringContent("test", "asics", "testContent")
            val uris = listOf(Uri.fromFile(file))

            `when`(fileOpeningRepository.uriToFile(context, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningRepository.isSivaConfirmationNeeded(context, listOf(file))).thenReturn(true)

            val isSivaConfirmationNeeded = viewModel.isSivaConfirmationNeeded(uris)

            assertTrue(isSivaConfirmationNeeded)
        }

    @Test
    fun fileOpeningViewModel_handleCancelDdocMimeType_activityIsFinished() =
        runTest {
            val mockActivity = mock(Activity::class.java)

            viewModel.handleCancelDdocMimeType(mockActivity, true)

            verify(mockActivity)?.finish()
        }

    @Test
    fun fileOpeningViewModel_handleCancelDdocMimeType_activityIsNotFinished() =
        runTest {
            val mockActivity = mock(Activity::class.java)

            viewModel.handleCancelDdocMimeType(mockActivity, false)

            verifyNoInteractions(mockActivity)
        }

    @Test
    fun fileOpeningViewModel_resetExternalFileState_externalFileStateIsReset() =
        runTest {
            val sharedContainerViewModel = SharedContainerViewModel(context, contentResolver)
            viewModel.resetExternalFileState(sharedContainerViewModel)

            assertNull(sharedContainerViewModel.externalFileUri.value)
        }

    private fun createTempFileWithStringContent(
        filename: String,
        fileExtension: String = "txt",
        content: String,
    ): File {
        val tempFile = File.createTempFile(filename, ".$fileExtension", context.cacheDir)
        Files.write(tempFile.toPath(), content.toByteArray(Charset.defaultCharset()))
        tempFile.deleteOnExit()
        return tempFile
    }
}
