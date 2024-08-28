@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.extensions

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.PDF_EXTENSION
import ee.ria.DigiDoc.common.Constant.PDF_MIMETYPE
import ee.ria.DigiDoc.common.Constant.SIGNATURE_CONTAINER_MIMETYPES
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.isDdoc
import ee.ria.DigiDoc.utilsLib.file.FileUtil.deleteFilesInFolder
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getFileInContainerZip
import ee.ria.DigiDoc.utilsLib.file.FileUtil.parseXMLFile
import ee.ria.DigiDoc.utilsLib.file.FileUtil.readFileAsString
import java.io.File
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipFile

private const val FILE_EXTENSIONS_LOG_TAG = "FileExtensions"

fun File.isPDF(context: Context): Boolean = PDF_MIMETYPE == mimeType(context) || PDF_EXTENSION == extension

fun File.mimeType(context: Context): String {
    val extension = extension.lowercase()

    val tempContainerFiles = File(context.filesDir, "tempContainerFiles")
    try {
        // Check if file is a zip file. If not, throw ZipException
        ZipFile(this)

        val mimetypeFile = getFileInContainerZip(this, "mimetype", tempContainerFiles)
        mimetypeFile?.let {
            return readFileAsString(it).also { _ -> deleteFilesInFolder(tempContainerFiles) }
        }
    } catch (ze: ZipException) {
        if (parseXMLFile(this)?.let { isDdoc(it) } == true) {
            return DDOC_MIMETYPE
        }

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?.takeIf { it.isNotEmpty() } ?: DEFAULT_MIME_TYPE
    }

    return DEFAULT_MIME_TYPE
}

fun File.isContainer(context: Context): Boolean {
    val mimetype = mimeType(context)

    return when {
        SIGNATURE_CONTAINER_MIMETYPES.contains(mimetype) -> true
        else -> parseXMLFile(this)?.let { isDdoc(it) } ?: false
    }
}

fun File.isSignedPDF(context: Context): Boolean {
    PDFBoxResourceLoader.init(context)
    return try {
        PDDocument.load(this).use { document ->
            document.getSignatureDictionaries().any { signature ->
                signature.filter == "Adobe.PPKLite" ||
                    signature.subFilter == "ETSI.CAdES.detached" ||
                    signature.subFilter == "adbe.pkcs7.detached"
            }
        }
    } catch (e: IOException) {
        Log.e(FILE_EXTENSIONS_LOG_TAG, "Unable to check if PDF is signed", e)
        false
    }
}
