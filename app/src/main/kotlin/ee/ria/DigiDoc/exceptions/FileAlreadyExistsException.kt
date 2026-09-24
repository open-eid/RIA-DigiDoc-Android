// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.exceptions

import android.content.Context

class FileAlreadyExistsException(
    private val context: Context,
    private val fileName: String,
) : Exception() {
    override fun getLocalizedMessage(): String =
        context.getString(
            ee.ria.DigiDoc.common.R.string.document_add_error_exists,
            fileName,
        )

    fun getFileName(): String = fileName
}
