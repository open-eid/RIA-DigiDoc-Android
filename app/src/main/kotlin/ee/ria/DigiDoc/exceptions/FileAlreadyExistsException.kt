@file:Suppress("PackageName")

package ee.ria.DigiDoc.exceptions

import android.content.Context

class FileAlreadyExistsException(
    private val context: Context,
) : Exception() {
    override fun getLocalizedMessage(): String =
        context.getString(
            ee.ria.DigiDoc.common.R.string.documents_add_error_exists,
        )
}
