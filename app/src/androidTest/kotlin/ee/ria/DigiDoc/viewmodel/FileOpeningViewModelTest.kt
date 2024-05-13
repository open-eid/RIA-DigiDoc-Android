@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

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

    private lateinit var viewModel: FileOpeningViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = FileOpeningViewModel(context, contentResolver, fileOpeningRepository)
        viewModel.signedContainer.observeForever(signedContainerObserver)
        viewModel.errorState.observeForever(errorStateObserver)
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

            viewModel.handleFiles(uris)

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

            viewModel.handleFiles(uris)

            verify(signedContainerObserver, atLeastOnce()).onChanged(signedContainer)
        }

    @Test
    fun fileOpeningViewModel_showFileChooser_success() =
        runTest {
            viewModel.showFileChooser(fileChooserLauncher)

            verify(fileOpeningRepository).showFileChooser(fileChooserLauncher, "*/*")
            assertFalse(viewModel.launchFilePicker.value)
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

            viewModel.handleFiles(uris)

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

            viewModel.handleFiles(uris)

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

            viewModel.handleFiles(uris)

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

            viewModel.handleFiles(uris)

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

            viewModel.handleFiles(uris)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(exception.message)
        }

    @Test
    fun fileOpeningViewModel_handleFiles_failWithEmptyFileList() =
        runTest {
            val uris = emptyList<Uri>()

            viewModel.handleFiles(uris)

            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(errorStateObserver).onChanged(null)
        }
}
