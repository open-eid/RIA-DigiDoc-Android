@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.methods

import androidx.annotation.StringRes
import ee.ria.DigiDoc.R

enum class SigningMethod(
    val methodName: String,
    @param:StringRes val label: Int,
) {
    NFC("NFC", R.string.signature_update_signature_add_method_nfc),
    ID_CARD("IDCard", R.string.signature_update_signature_add_method_id_card),
    MOBILE_ID("MobileId", R.string.signature_update_signature_add_method_mobile_id),
    SMART_ID("SmartId", R.string.signature_update_signature_add_method_smart_id),
}
