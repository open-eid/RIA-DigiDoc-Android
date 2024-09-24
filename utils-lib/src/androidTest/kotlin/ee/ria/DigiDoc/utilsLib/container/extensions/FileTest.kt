@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container.extensions

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.webkit.MimeTypeMap
import androidx.test.platform.app.InstrumentationRegistry
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import ee.ria.DigiDoc.common.Constant.ASICE_MIMETYPE
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.PDF_EXTENSION
import ee.ria.DigiDoc.common.testfiles.file.TestFileUtil.Companion.createZipWithTextFile
import ee.ria.DigiDoc.utilsLib.extensions.isCades
import ee.ria.DigiDoc.utilsLib.extensions.isContainer
import ee.ria.DigiDoc.utilsLib.extensions.isPDF
import ee.ria.DigiDoc.utilsLib.extensions.isSignedPDF
import ee.ria.DigiDoc.utilsLib.extensions.isXades
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class FileTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Mock
    private lateinit var mimeTypeMap: MimeTypeMap

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun file_mimeType_success() {
        val file: File = createZipWithTextFile(ASICE_MIMETYPE, "mimetype")

        val fileMimeType = file.mimeType(context)

        assertEquals(ASICE_MIMETYPE, fileMimeType)
    }

    @Test
    fun file_isPDF_success() {
        val file = mockFile("test", PDF_EXTENSION, "application/pdf")

        assertTrue(file.isPDF(context))
        assertEquals("application/pdf", file.mimeType(context))
    }

    @Test
    fun file_isPDF_returnFalseWithMimeTypeWrong() {
        val file = mockFile("test", "txt", "text/plain")

        assertFalse(file.isPDF(context))
    }

    @Test
    fun file_isContainer_success() {
        val file = createZipWithTextFile(ASICE_MIMETYPE, "mimetype")

        assertTrue(file.isContainer(context))
    }

    @Test
    fun file_isContainer_returnFalseWhenFileNotContainer() {
        val file = mockFile("test", "jpg", "image/jpg")

        assertFalse(file.isContainer(context))
    }

    @Test
    fun file_mimeType_successForNonContainerExtension() {
        val file = mockFile("test", "pdf", "application/pdf")

        assertEquals("application/pdf", file.mimeType(context))
        assertTrue(file.isPDF(context))
    }

    @Test
    fun file_mimeType_returnDefaultMimeTypeForUnknownExtension() {
        val file = mockFile("test", "qwerty", "")

        assertEquals(DEFAULT_MIME_TYPE, file.mimeType(context))
    }

    @Test
    fun file_mimeType_successWithCaseInsensitiveExtensions() {
        val file = mockFile("testFile", "PNG", "image/png")

        val mimeType = file.mimeType(context)

        assertEquals("image/png", mimeType)
    }

    @Test
    fun file_isSignedPDF_success() {
        val signedTestPDFFile = createSignedPDF()

        val isSignedPDF = signedTestPDFFile?.isSignedPDF(context)

        if (isSignedPDF != null) {
            assertTrue(isSignedPDF)
            assertEquals("application/pdf", signedTestPDFFile.mimeType(context))
            assertTrue(signedTestPDFFile.isPDF(context))
        } else {
            fail("Signed PDF is null")
        }
    }

    @Test
    fun file_isPDF_successSignedIsPDF() {
        val signedTestPDFFile = createSignedPDF()

        val isPdf = signedTestPDFFile?.isPDF(context)

        if (signedTestPDFFile != null && isPdf != null) {
            assertTrue(isPdf)
            assertTrue(signedTestPDFFile.isSignedPDF(context))
            assertEquals("application/pdf", signedTestPDFFile.mimeType(context))
        } else {
            fail("PDF file is null")
        }
    }

    @Test
    fun file_isXades_success() {
        val testFile = createZipWithTextFile(ASICS_MIMETYPE, "signatures.xml")

        val isXades = testFile?.isXades(context)

        if (isXades != null) {
            assertTrue(isXades)
        } else {
            fail("isXades is null")
        }
    }

    fun file_isCades_success() {
        val testFile = createZipWithTextFile(ASICE_MIMETYPE, "signature001.p7s")

        val isCades = testFile?.isCades(context)

        if (isCades != null) {
            assertTrue(isCades)
        } else {
            fail("isCades is null")
        }
    }

    @Test
    fun file_isXades_falseWithoutXadesSignature() {
        val testFile = createZipWithTextFile(ASICS_MIMETYPE, "testFile.txt")

        val isXades = testFile?.isXades(context)

        if (isXades != null) {
            assertFalse(isXades)
        } else {
            fail("isXades is null")
        }
    }

    fun file_isCades_falseWithoutCadesSignature() {
        val testFile = createZipWithTextFile(ASICE_MIMETYPE, "testFile.txt")

        val isCades = testFile?.isCades(context)

        if (isCades != null) {
            assertFalse(isCades)
        } else {
            fail("isCades is null")
        }
    }

    private fun mockFile(
        fileName: String,
        fileExtension: String,
        mimeType: String,
    ): File {
        val tempFile = File.createTempFile(fileName, ".$fileExtension", context.cacheDir)
        tempFile.deleteOnExit()
        val file = mock(File::class.java)
        `when`(file.name).thenReturn("$fileName.$fileExtension")
        `when`(file.path).thenReturn(tempFile.path)

        `when`(mimeTypeMap.getMimeTypeFromExtension(file.extension.lowercase())).thenReturn(mimeType)

        return file
    }

    private fun createSignedPDF(): File? {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(100, 100, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        pdfDocument.finishPage(page)

        val testPDFFile = File.createTempFile("testFile", ".$PDF_EXTENSION", context.cacheDir)
        val signedTestPDFFile = File.createTempFile("signedTestFile", ".$PDF_EXTENSION", context.cacheDir)
        testPDFFile.delete()
        signedTestPDFFile.delete()
        try {
            val fileOutputStream = FileOutputStream(testPDFFile)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        try {
            val document = PDDocument.load(testPDFFile)

            val signature = PDSignature()
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED)
            signature.name = "Test Signature"
            signature.location = "Location"
            signature.reason = "Reason"

            document.addSignature(signature)

            document.save(signedTestPDFFile)
            document.close()
            return signedTestPDFFile
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}
