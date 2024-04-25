@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.libdigidocpp.DataFile

class DataFileWrapper(dataFile: DataFile) : DataFileInterface {
    override val fileName: String = dataFile.fileName()
    override val fileSize: Long = dataFile.fileSize()
    override val mediaType: String = dataFile.mediaType()
}
