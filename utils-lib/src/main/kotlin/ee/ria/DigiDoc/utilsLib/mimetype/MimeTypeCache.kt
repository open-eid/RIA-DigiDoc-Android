@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.mimetype

import java.io.File

interface MimeTypeCache {
    fun getMimeType(file: File): String

    fun setMimeType(
        md5: String,
        mimeType: String,
    )
}
