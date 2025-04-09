@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.exception

import android.content.Context
import ee.ria.DigiDoc.common.R

class NoInternetConnectionException(
    private val context: Context,
) : Exception() {
    override fun getLocalizedMessage(): String =
        context.getString(
            R.string.no_internet_connection,
        )
}
