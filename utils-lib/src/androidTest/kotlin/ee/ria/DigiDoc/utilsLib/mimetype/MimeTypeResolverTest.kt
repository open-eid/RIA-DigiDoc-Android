@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.mimetype

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File

class MimeTypeResolverTest {
    private lateinit var context: Context

    @Mock
    private lateinit var mockMimeTypeCache: MimeTypeCache

    private lateinit var mimeTypeResolver: MimeTypeResolver

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mimeTypeResolver = MimeTypeResolverImpl(mockMimeTypeCache)
    }

    @Test
    fun mimeTypeResolver_mimeType_success() {
        val file = File("testfile.txt")
        val expectedMimeType = "text/plain"

        `when`(mockMimeTypeCache.getMimeType(file)).thenReturn(expectedMimeType)

        val result = mimeTypeResolver.mimeType(file)

        assertEquals(expectedMimeType, result)
        verify(mockMimeTypeCache).getMimeType(file)
    }

    @Test
    fun mimeTypeResolver_mimeType_returnDefaultMimetypeWhenFileHasEmptyMimetype() {
        val file = File("testfile.txt")

        `when`(mockMimeTypeCache.getMimeType(file)).thenReturn("")

        val result = mimeTypeResolver.mimeType(file)

        assertEquals(DEFAULT_MIME_TYPE, result)
        verify(mockMimeTypeCache).getMimeType(file)
    }

    @Test
    fun mimeTypeResolver_mimeType_returnNullWhenFileNull() {
        val result = mimeTypeResolver.mimeType(null)

        assertNull(result)
    }
}
