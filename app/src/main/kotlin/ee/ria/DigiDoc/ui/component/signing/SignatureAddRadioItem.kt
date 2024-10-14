@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.Route

data class SignatureAddRadioItem(
    @StringRes val label: Int = 0,
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = "",
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun radioItems(): List<SignatureAddRadioItem> {
        return listOf(
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_nfc,
                route = Route.NFC.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_nfc,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodNFC",
            ),
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_id_card,
                route = Route.IdCard.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_id_card,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodIdCard",
            ),
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_mobile_id,
                route = Route.MobileId.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_mobile_id,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodMobileId",
            ),
            SignatureAddRadioItem(
                label = R.string.signature_update_signature_add_method_smart_id,
                route = Route.SmartId.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_smart_id,
                    ).lowercase(),
                testTag = "signatureUpdateSignatureAddMethodSmartId",
            ),
        )
    }
}
