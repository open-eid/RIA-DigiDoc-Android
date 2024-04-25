@file:Suppress("PackageName")

package ee.ria.DigiDoc.exceptions

import android.content.Context
import ee.ria.DigiDoc.R

class EmptyFileException(context: Context) : Exception(
    context.getString(R.string.empty_file_error),
)
