@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.exceptions

import android.content.Context

class NoInternetConnectionException(
    private val context: Context,
) : Exception() {
    override fun getLocalizedMessage(): String =
        context.getString(
            ee.ria.DigiDoc.common.R.string.no_internet_connection,
        )
}
