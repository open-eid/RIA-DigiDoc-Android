@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import ee.ria.DigiDoc.domain.model.pin.PinChoice

data class PinChangeContent(
    val title: Int,
    val pinChoice: PinChoice,
    val isForgottenPin: Boolean = false,
)
