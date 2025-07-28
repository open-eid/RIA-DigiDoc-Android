@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.myeid

import androidx.annotation.StringRes
import ee.ria.DigiDoc.R

enum class MyEidIdentificationMethodSetting(
    val methodName: String,
    @param:StringRes val label: Int,
) {
    NFC("NFC", R.string.signature_update_signature_add_method_nfc),
    ID_CARD("IDCard", R.string.signature_update_signature_add_method_id_card),
    ;

    companion object {
        fun fromMethod(mode: String): MyEidIdentificationMethodSetting = entries.find { it.methodName == mode } ?: NFC
    }
}
