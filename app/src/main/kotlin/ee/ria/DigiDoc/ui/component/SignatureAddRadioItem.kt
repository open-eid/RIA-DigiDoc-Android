@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.Route

data class SignatureAddRadioItem(
    val label: String = "",
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = "",
    val contentDescription: String = "",
) {
    @Composable
    fun radioItems(): List<SignatureAddRadioItem> {
        return listOf(
            SignatureAddRadioItem(
                label = stringResource(id = R.string.signature_update_signature_add_method_mobile_id),
                route = Route.MobileId.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_mobile_id,
                    ).lowercase(),
            ),
            SignatureAddRadioItem(
                label = stringResource(id = R.string.signature_update_signature_add_method_smart_id),
                route = Route.SmartId.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_smart_id,
                    ).lowercase(),
            ),
            SignatureAddRadioItem(
                label = stringResource(id = R.string.signature_update_signature_add_method_id_card),
                route = Route.IdCard.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_id_card,
                    ).lowercase(),
            ),
            SignatureAddRadioItem(
                label = stringResource(id = R.string.signature_update_signature_add_method_nfc),
                route = Route.NFC.route,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_nfc,
                    ).lowercase(),
            ),
        )
    }
}
