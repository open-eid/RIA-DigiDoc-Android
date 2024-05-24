@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.response

data class SessionStatusResponse(
    val state: SessionStatusResponseProcessState?,
    val result: SessionStatusResponseStatus?,
    val signature: SmartSignatureResponse?,
    val cert: SmartCertificateResponse?,
)
