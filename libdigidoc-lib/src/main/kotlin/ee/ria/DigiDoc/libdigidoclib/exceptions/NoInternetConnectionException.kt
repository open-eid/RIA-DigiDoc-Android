@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.exceptions

import android.content.Context
import ee.ria.DigiDoc.common.R.string.no_internet_connection

class NoInternetConnectionException(context: Context) : Exception(
    context.getString(no_internet_connection),
)
