// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.libdigidocpp.DataFile
import java.io.Serializable

class DataFileWrapper(
    dataFile: DataFile,
) : DataFileInterface,
    Serializable {
    override val id: String = dataFile.id()
    override val fileName: String = dataFile.fileName()
    override val fileSize: Long = dataFile.fileSize()
    override val mediaType: String = dataFile.mediaType()
}
