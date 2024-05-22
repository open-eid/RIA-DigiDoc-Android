@file:Suppress("PackageName")

package ee.ria.DigiDoc.exceptions

import android.content.Context
import ee.ria.DigiDoc.R

class FileAlreadyExistsException(context: Context) : Exception(
    context.getString(R.string.signature_update_documents_add_error_exists),
)
