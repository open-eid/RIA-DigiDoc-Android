@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.mimetype

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.ASICE_MIMETYPE
import ee.ria.DigiDoc.common.testfiles.file.TestFileUtil.Companion.createZipWithTextFile
import ee.ria.DigiDoc.utilsLib.extensions.md5Hash
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File

class MimeTypeCacheTest {
    private lateinit var context: Context
    private lateinit var mimeTypeCache: MimeTypeCache

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mimeTypeCache = MimeTypeCacheImpl(context)
    }

    @Test
    fun mimeTypeCache_getMimeType_success() {
        val file: File = createZipWithTextFile(ASICE_MIMETYPE, "mimetype")

        val fileMimeType = mimeTypeCache.getMimeType(file)

        assertEquals(ASICE_MIMETYPE, fileMimeType)
    }

    @Test
    fun mimeTypeCache_setMimeType_success() {
        val file = createZipWithTextFile(ASICE_MIMETYPE)
        val md5 = file.md5Hash()
        val mimeType = ASICE_MIMETYPE

        mimeTypeCache.setMimeType(md5, mimeType)

        val fileMimetype = mimeTypeCache.getMimeType(file)

        assertEquals(mimeType, fileMimetype)
    }

    @Test
    fun mimeTypeCache_getMimeType_returnEmptyMimetypeWhenFileHasUnknownMimetype() {
        val file = createZipWithTextFile("randomMimetype")

        val mimetype = mimeTypeCache.getMimeType(file)

        assertNotNull(mimetype)
    }
}
