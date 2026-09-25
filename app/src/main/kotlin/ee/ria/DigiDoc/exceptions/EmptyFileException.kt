// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.exceptions

import android.content.Context

class EmptyFileException(
    private val context: Context,
) : Exception() {
    override fun getLocalizedMessage(): String = context.getString(ee.ria.DigiDoc.common.R.string.empty_file_error)
}
