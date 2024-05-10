@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

interface DataFileInterface {
    val id: String
    val fileName: String
    val fileSize: Long
    val mediaType: String
}
