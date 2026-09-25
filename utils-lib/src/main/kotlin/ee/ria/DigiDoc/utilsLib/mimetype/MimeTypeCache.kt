// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
