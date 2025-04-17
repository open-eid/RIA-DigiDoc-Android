@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.crypto

import androidx.annotation.StringRes
import ee.ria.DigiDoc.R

enum class DecryptMethodSetting(
    val methodName: String,
    @StringRes val label: Int,
) {
    NFC("NFC", R.string.signature_update_signature_add_method_nfc),
    ID_CARD("IDCard", R.string.signature_update_signature_add_method_id_card),
    ;

    companion object {
        fun fromMethod(mode: String): DecryptMethodSetting {
            return entries.find { it.methodName == mode } ?: NFC
        }
    }
}
