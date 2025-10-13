@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.exception

class WebEidException(
    val errorCode: WebEidErrorCode,
    override val message: String,
    val responseUri: String,
) : Exception()
