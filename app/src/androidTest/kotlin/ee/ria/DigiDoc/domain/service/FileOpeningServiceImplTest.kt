@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class FileOpeningServiceImplTest {
    private lateinit var fileOpeningService: FileOpeningServiceImpl
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setUp() {
        fileOpeningService = FileOpeningServiceImpl()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = context.contentResolver
    }

    @Test
    fun fileOpeningService_isFileSizeValid_validFileSuccess() {
        val file = createTempFile()
        runBlocking {
            val isValid = fileOpeningService.isFileSizeValid(file)
            assertTrue(isValid)
        }
    }

    @Test
    fun fileOpeningService_isFileSizeValid_invalidFileUnsuccessful() {
        val file = createTempFile()
        file.delete()
        runBlocking {
            val isValid = fileOpeningService.isFileSizeValid(file)
            assertFalse(isValid)
        }
    }

    @Test
    fun fileOpeningService_isFileSizeValid_size0ReturnFalse() {
        val file = File.createTempFile("test", ".txt", context.cacheDir)
        runBlocking {
            val isValid = fileOpeningService.isFileSizeValid(file)
            assertFalse(isValid)
        }
    }

    @Test
    fun fileOpeningService_isFileSizeValid_throwException() =
        runTest {
            val file = mock(File::class.java)
            `when`(file.exists()).thenAnswer {
                if (file.isFile && file.length() > 0) {
                    throw Exception("An exception")
                } else {
                    it.getArgument(0)
                }
            }

            val isValid = fileOpeningService.isFileSizeValid(file)
            assertFalse(isValid)
        }

    @Test
    fun fileOpeningService_isFileSizeValid_mockIsFileFalseReturnFalse() =
        runTest {
            val file = mock(File::class.java)

            `when`(file.exists()).thenReturn(true)
            `when`(file.isFile).thenReturn(false)
            `when`(file.length()).thenReturn(24)

            val isValid = fileOpeningService.isFileSizeValid(file)
            assertFalse(isValid)
        }

    @Test
    fun fileOpeningService_isFileSizeValid_mockFileExistsFalseReturnFalse() =
        runTest {
            val file = mock(File::class.java)

            `when`(file.exists()).thenReturn(false)
            `when`(file.isFile).thenReturn(true)
            `when`(file.length()).thenReturn(24)

            val isValid = fileOpeningService.isFileSizeValid(file)
            assertFalse(isValid)
        }

    @Test
    fun fileOpeningService_isFileSizeValid_mockFileLength0ReturnFalse() =
        runTest {
            val file = mock(File::class.java)

            `when`(file.exists()).thenReturn(true)
            `when`(file.isFile).thenReturn(true)
            `when`(file.length()).thenReturn(0)

            val isValid = fileOpeningService.isFileSizeValid(file)
            assertFalse(isValid)
        }

    @Test
    fun fileOpeningService_uriToFile_success() {
        val uri = Uri.fromFile(createTempFile())

        runBlocking {
            val file = fileOpeningService.uriToFile(context, contentResolver, uri)
            assertTrue(file.exists())
            assertTrue(file.isFile)
        }
    }

    private fun createTempFile(): File {
        val tempFile = File.createTempFile("test", ".txt", context.cacheDir)
        Files.write(tempFile.toPath(), "Temp file".toByteArray(Charset.defaultCharset()))
        return tempFile
    }
}
