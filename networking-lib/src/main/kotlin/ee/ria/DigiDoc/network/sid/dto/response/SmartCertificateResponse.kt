@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.response

data class SmartCertificateResponse(
    val value: String?,
    val assuranceLevel: String?,
    val certificateLevel: String?,
)
