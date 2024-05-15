@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
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
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class SharedContainerViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    private lateinit var viewModel: SharedContainerViewModel

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
        viewModel = SharedContainerViewModel(context, contentResolver)
    }

    @Test
    fun sharedContainerViewModel_saveContainerFile_success() {
        val file = createTempFileWithStringContent("test", "Test content")
        val activityResult = ActivityResult(0, mock(Intent::class.java))
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
    fun sharedContainerViewModel_removeContainerDataFile_success() {
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

        val container = viewModel.removeContainerDataFile(signedContainer, dataFile)

        if (container != null) {
            assertEquals(1, container.getDataFiles().size)
        }
    }

    @Test(expected = ContainerDataFilesEmptyException::class)
    fun sharedContainerViewModel_removeContainerDataFile_throwsException() {
        val file = createTempFileWithStringContent("test", "Test content")
        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, file, listOf(file))
            }

        val dataFile = signedContainer.getDataFiles().first()

        viewModel.removeContainerDataFile(signedContainer, dataFile)
    }

    @Test
    fun sharedContainerViewModel_removeContainerDataFile_returnNullIfDataFileNull() {
        val file = createTempFileWithStringContent("test", "Test content")
        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, file, listOf(file))
            }

        val container = viewModel.removeContainerDataFile(signedContainer, null)

        assertNull(container)
    }

    @Test
    fun sharedContainerViewModel_removeContainerDataFile_returnNullIfContainerNull() {
        val file = createTempFileWithStringContent("test", "Test content")
        val signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, file, listOf(file))
            }

        val dataFile = signedContainer.getDataFiles().first()

        val container = viewModel.removeContainerDataFile(null, dataFile)

        assertNull(container)
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
