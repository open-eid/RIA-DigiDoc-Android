@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

import java.util.Date

data class SignatureItem(
    val name: String = "",
    val status: String = "",
    val signedDate: Date = Date(),
)
