@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.extensions

import android.webkit.MimeTypeMap
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.PDF_EXTENSION
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class FileTest {
    @Mock
    private lateinit var mimeTypeMap: MimeTypeMap

    private val mockedStatic: MockedStatic<MimeTypeMap> = mockStatic(MimeTypeMap::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockedStatic.`when`<MimeTypeMap> { MimeTypeMap.getSingleton() }.thenReturn(mimeTypeMap)
    }

    @After
    fun tearDown() {
        mockedStatic.close()
    }

    @Test
    fun file_isPDF_success() {
        val file = mockFile("test.$PDF_EXTENSION", "application/pdf")

        assertEquals(true, file.isPDF)
        assertEquals("application/pdf", file.mimeType)
    }

    @Test
    fun file_isPDF_returnFalseWithMimeTypeWrong() {
        val file = mockFile("test.txt", "text/plain")

        assertEquals(false, file.isPDF)
    }

    @Test
    fun file_isContainer_success() {
        val file = mockFile("test.asice", "application/vnd.etsi.asic-e+zip")

        assertEquals(true, file.isContainer)
    }

    @Test
    fun file_isContainer_returnFalseWhenFileExtensionNotContainer() {
        val file = mockFile("test.jpg", "image/jpg")

        assertEquals(false, file.isContainer)
    }

    @Test
    fun file_mimeType_success() {
        val file = mockFile("test.asice", "application/octet-stream")

        assertEquals(CONTAINER_MIME_TYPE, file.mimeType)
    }

    @Test
    fun file_mimeType_successForNonContainerExtension() {
        val file = mockFile("test.pdf", "application/pdf")

        assertEquals("application/pdf", file.mimeType)
    }

    @Test
    fun file_mimeType_returnDefaultMimeTypeForUnknownExtension() {
        val file = mockFile("test.qwerty", "")

        assertEquals(DEFAULT_MIME_TYPE, file.mimeType)
    }

    @Test
    fun file_mimeType_successWithCaseInsensitiveExtensions() {
        val file = mockFile("testFile.PNG", "image/png")

        val mimeType = file.mimeType

        assertEquals("image/png", mimeType)
    }

    private fun mockFile(
        fileName: String,
        mimeType: String,
    ): File {
        val file = mock(File::class.java)
        `when`(file.name).thenReturn(fileName)

        mockedStatic.`when`<MimeTypeMap> { MimeTypeMap.getSingleton() }.thenReturn(mimeTypeMap)
        `when`(mimeTypeMap.getMimeTypeFromExtension(file.extension.lowercase())).thenReturn(mimeType)

        return file
    }
}
