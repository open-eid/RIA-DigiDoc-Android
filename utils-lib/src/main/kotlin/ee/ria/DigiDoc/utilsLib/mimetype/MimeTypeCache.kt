@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.mimetype

interface MimeTypeCache {
    fun getMimeType(fileUri: String): String

    fun setMimeType(
        md5: String,
        mimeType: String,
    )
}
