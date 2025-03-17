@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.methods.SigningMethod

data class SignatureAddRadioItem(
    @StringRes val label: Int = 0,
    val icon: ImageVector = Icons.Filled.Home,
    val method: SigningMethod = SigningMethod.NFC,
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun radioItems(): List<SignatureAddRadioItem> {
        return listOf(
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_nfc,
                method = SigningMethod.NFC,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_nfc,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodNFC",
            ),
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_id_card,
                method = SigningMethod.ID_CARD,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_id_card,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodIdCard",
            ),
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_mobile_id,
                method = SigningMethod.MOBILE_ID,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_mobile_id,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodMobileId",
            ),
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_smart_id,
                method = SigningMethod.SMART_ID,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_smart_id,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodSmartId",
            ),
        )
    }
}
