@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.exceptions

import android.content.Context
import ee.ria.DigiDoc.common.R

class SSLHandshakeException(
    context: Context,
    message: String? = context.getString(R.string.invalid_ssl_handshake),
) : Exception(message)
