@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.mimetype

import java.io.File

interface MimeTypeResolver {
    fun mimeType(file: File?): String
}
