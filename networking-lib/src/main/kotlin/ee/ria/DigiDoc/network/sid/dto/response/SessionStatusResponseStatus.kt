@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.response

data class SessionStatusResponseStatus(
    val endResult: SessionStatusResponseProcessStatus?,
    val documentNumber: String?,
)
