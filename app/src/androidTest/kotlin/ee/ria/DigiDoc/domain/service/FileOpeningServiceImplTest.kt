@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

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

    @Test(expected = Exception::class)
    fun fileOpeningService_isFileSizeValid_throwException() {
        val file = createTempFile()
        `when`(file.exists()).thenThrow(Exception("An exception"))

        assertThrows(Exception::class.java) {
            runTest {
                fileOpeningService.isFileSizeValid(file)
                verify(LoggingUtil).errorLog(anyString(), anyString())
            }
        }
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
