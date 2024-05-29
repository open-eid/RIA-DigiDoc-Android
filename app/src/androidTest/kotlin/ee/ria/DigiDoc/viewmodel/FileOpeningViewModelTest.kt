@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.net.toFile
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.exceptions.FileAlreadyExistsException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyList
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class FileOpeningViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    @Mock
    lateinit var fileChooserLauncher: ActivityResultLauncher<String>

    @Mock
    lateinit var signedContainerObserver: Observer<SignedContainer?>

    @Mock
    lateinit var errorStateObserver: Observer<String?>

    @Mock
    lateinit var launchFilePickerObserver: Observer<Boolean?>

    private lateinit var viewModel: FileOpeningViewModel

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
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = FileOpeningViewModel(context, contentResolver, fileOpeningRepository)
        viewModel.signedContainer.observeForever(signedContainerObserver)
        viewModel.errorState.observeForever(errorStateObserver)
        viewModel.launchFilePicker.observeForever(launchFilePickerObserver)
    }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_success() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", "Test content")
            val anotherFile = createTempFileWithStringContent("test2", "Another file")
            val newFiles = listOf(anotherFile)

            val existingSignedContainer = SignedContainer.openOrCreate(context, file, listOf(file))

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.addFilesToContainer(context, newFiles)
                }

            `when`(
                fileOpeningRepository.isEmptyFileInList(anyList()),
            )
                .thenReturn(false)

            `when`(
                fileOpeningRepository.getFilesWithValidSize(anyList()),
            )
                .thenReturn(newFiles)

            `when`(
                fileOpeningRepository.addFilesToContainer(
                    any(),
                    anyList(),
                ),
            )
                .thenReturn(signedContainer)

            viewModel.handleFiles(uris, existingSignedContainer)

            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_throwException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", "Test content")
            val anotherFile = createTempFileWithStringContent("test2", "Another file")
            val newFiles = listOf(anotherFile)

            val uriToFile = fileOpeningRepository.uriToFile(context, contentResolver, uri)

            val existingSignedContainer = SignedContainer.openOrCreate(context, file, listOf(file))

            val exception = Exception("Could not add files to container")

            `when`(
                fileOpeningRepository.isFileSizeValid(uriToFile),
            )
                .thenReturn(true)

            `when`(
                fileOpeningRepository.isEmptyFileInList(anyList()),
            )
                .thenReturn(false)

            `when`(
                fileOpeningRepository.getFilesWithValidSize(anyList()),
            )
                .thenReturn(newFiles)

            `when`(
                fileOpeningRepository.addFilesToContainer(
                    any(),
                    anyList(),
                ),
            )
                .thenThrow(exception)
            viewModel.handleFiles(uris, existingSignedContainer)

            verify(signedContainerObserver, atLeastOnce()).onChanged(existingSignedContainer)
            verify(errorStateObserver, atLeastOnce()).onChanged(exception.message)
        }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_throwEmptyFileException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", "Test content")

            val existingSignedContainer = SignedContainer.openOrCreate(context, file, listOf(file))

            val uriToFile = fileOpeningRepository.uriToFile(context, contentResolver, uri)

            `when`(
                fileOpeningRepository.isFileSizeValid(uriToFile),
            )
                .thenReturn(false)

            viewModel.handleFiles(uris, existingSignedContainer)

            verify(signedContainerObserver, atLeastOnce()).onChanged(existingSignedContainer)
            verify(errorStateObserver, atLeastOnce()).onChanged("Cannot add empty file to the container.")
        }

    @Test
    fun fileOpeningViewModel_handleFilesWithExistingContainer_throwFileAlreadyExistsException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val file = createTempFileWithStringContent("test", "Test content")

            val uriToFile = fileOpeningRepository.uriToFile(context, contentResolver, uri)

            val existingSignedContainer = SignedContainer.openOrCreate(context, file, listOf(file))

            `when`(
                fileOpeningRepository.isFileSizeValid(uriToFile),
            )
                .thenReturn(true)

            `when`(
                fileOpeningRepository.isFileAlreadyInContainer(uriToFile, existingSignedContainer),
            )
                .thenReturn(true)

            viewModel.handleFiles(uris, existingSignedContainer)

            verify(signedContainerObserver, atLeastOnce()).onChanged(existingSignedContainer)
            verify(errorStateObserver, atLeastOnce()).onChanged("File already exists in the container")
        }

    @Test
    fun fileOpeningViewModel_handleFiles_success() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val signedContainer =
                runBlocking {
                    fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris)
                }

            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    context,
                    contentResolver,
                    uris,
                ),
            )
                .thenReturn(signedContainer)

            viewModel.handleFiles(uris, null)

            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_successWithMultipleURIs() =
        runTest {
            val uri1: Uri = mock()
            val uri2: Uri = mock()
            val uris = listOf(uri1, uri2)
            val signedContainer =
                runBlocking {
                    fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris)
                }

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris))
                .thenReturn(signedContainer)

            viewModel.handleFiles(uris, null)

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
            val exception = EmptyFileException(context)

            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    context,
                    contentResolver,
                    uris,
                ),
            )
                .thenThrow(exception)

            viewModel.handleFiles(uris, null)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver, atLeastOnce()).onChanged(exception.message)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_noSuchElementException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val exception = NoSuchElementException("No such element")

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris))
                .thenThrow(exception)

            viewModel.handleFiles(uris, null)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(exception.message)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_noInternetConnectionException() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val exception = NoInternetConnectionException(context)

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris))
                .thenThrow(exception)

            viewModel.handleFiles(uris, null)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(exception.message)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failWhenNullContainer() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris))
                .thenReturn(null)

            viewModel.handleFiles(uris, null)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failWhenExceptionThrown() =
        runTest {
            val uri: Uri = mock()
            val uris = listOf(uri)
            val exception = Exception("Could not load selected files")

            `when`(fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris))
                .thenThrow(exception)

            viewModel.handleFiles(uris, null)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(exception.message)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failWithEmptyFileList() =
        runTest {
            val uris = emptyList<Uri>()

            viewModel.handleFiles(uris, null)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(null)
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
