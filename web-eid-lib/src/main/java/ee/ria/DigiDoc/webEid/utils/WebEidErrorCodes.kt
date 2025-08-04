@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

object WebEidErrorCodes {
    const val INVALID_REQUEST = "ERR_WEBEID_INVALID_REQUEST"
    const val UNKNOWN = "ERR_WEBEID_UNKNOWN"

    const val INVALID_REQUEST_MESSAGE = "Invalid authentication request"
    const val UNKNOWN_MESSAGE = "Unexpected error occurred"
}
