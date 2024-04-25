@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.extensions

import android.webkit.MimeTypeMap
import ee.ria.DigiDoc.common.Constant.CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.PDF_EXTENSION
import java.io.File

val File.isPDF: Boolean
    get() {
        return PDF_EXTENSION == extension
    }

val File.isContainer: Boolean
    get() {
        return CONTAINER_EXTENSIONS.contains(extension)
    }

val File.mimeType: String
    get() {
        val extension: String = extension.lowercase()
        if (CONTAINER_EXTENSIONS.contains(extension)) {
            return CONTAINER_MIME_TYPE
        }
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType ?: DEFAULT_MIME_TYPE
    }
