@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import ee.ria.DigiDoc.idcard.CodeType

data class PinChangeContent(
    val title: Int,
    val codeType: CodeType,
    val isForgottenPin: Boolean = false,
)
