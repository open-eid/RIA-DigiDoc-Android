// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import java.io.Serializable

interface DataFileInterface : Serializable {
    val id: String
    val fileName: String
    val fileSize: Long
    val mediaType: String
}
