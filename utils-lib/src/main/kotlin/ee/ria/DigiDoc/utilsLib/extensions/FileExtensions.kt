/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.extensions

import android.content.Context
import android.webkit.MimeTypeMap
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import ee.ria.DigiDoc.common.Constant.CDOC1_EXTENSION
import ee.ria.DigiDoc.common.Constant.CDOC2_EXTENSION
import ee.ria.DigiDoc.common.Constant.DDOC_EXTENSION
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
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Locale
import java.util.zip.ZipException

private const val FILE_EXTENSIONS_LOG_TAG = "FileExtensions"

fun File.isPDF(context: Context): Boolean = PDF_MIMETYPE == mimeType(context) || PDF_EXTENSION == extension

fun File.isXades(context: Context): Boolean {
    val tempContainerFiles = File(context.filesDir, "tempContainerFiles")

    try {
        checkIsZipFile(this)

        val signaturesXmlFile = getFileInContainerZip(this, "signatures.xml", tempContainerFiles)
        val fileExists = signaturesXmlFile?.exists()

        deleteFilesInFolder(tempContainerFiles)
        return fileExists ?: false
    } catch (ze: ZipException) {
        debugLog(FILE_EXTENSIONS_LOG_TAG, "File is not a Xades container", ze)
        return false
    }
}

fun File.isCades(context: Context): Boolean {
    val tempContainerFiles = File(context.filesDir, "tempContainerFiles")

    try {
        checkIsZipFile(this)

        val signaturesXmlFile = getFileInContainerZip(this, "p7s", tempContainerFiles)
        val fileExists = signaturesXmlFile?.exists()

        deleteFilesInFolder(tempContainerFiles)
        return fileExists ?: false
    } catch (ze: ZipException) {
        debugLog(FILE_EXTENSIONS_LOG_TAG, "File is not a Cades container", ze)
        return false
    }
}

fun File.mimeType(context: Context): String {
    val extension = extension.lowercase()

    val tempContainerFiles = File(context.filesDir, "tempContainerFiles")
    try {
        checkIsZipFile(this)

        val mimetypeFile = getFileInContainerZip(this, "mimetype", tempContainerFiles)
        mimetypeFile?.let {
            return readFileAsString(it).also { _ -> deleteFilesInFolder(tempContainerFiles) }.trim()
        }
    } catch (_: ZipException) {
        if (extension == DDOC_EXTENSION && parseXMLFile(this)?.let { isDdoc(it) } == true) {
            return DDOC_MIMETYPE
        }
    }

    return MimeTypeMap
        .getSingleton()
        .getMimeTypeFromExtension(extension)
        ?.takeIf { it.isNotEmpty() } ?: DEFAULT_MIME_TYPE
}

fun File.isCryptoContainer(): Boolean {
    val extension: String = this.extension.lowercase(Locale.getDefault())
    return CDOC1_EXTENSION == extension || CDOC2_EXTENSION == extension
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
        errorLog(FILE_EXTENSIONS_LOG_TAG, "Unable to check if PDF is signed", e)
        false
    }
}

fun File.md5Hash(): String {
    try {
        FileInputStream(this).use { fis ->
            return DigestUtils.md5Hex(fis)
        }
    } catch (e: IOException) {
        errorLog(FILE_EXTENSIONS_LOG_TAG, "Unable to get MD5 of file: ${this.name}", e)
        return ""
    }
}

fun File.saveAs(destinationPath: String) {
    this.copyTo(File(destinationPath), overwrite = true)
}

// Check if file is a zip file. If not, throw ZipException
@Throws(ZipException::class)
private fun checkIsZipFile(file: File) {
    ZipFile
        .Builder()
        .setFile(file)
        .get()
}
