@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.crypto.DecryptMethodSetting

data class DecryptMethodChoiceButtonItem(
    @param:StringRes val label: Int = 0,
    val setting: DecryptMethodSetting = DecryptMethodSetting.NFC,
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun radioItems(): List<DecryptMethodChoiceButtonItem> =
        listOf(
            DecryptMethodChoiceButtonItem(
                label = R.string.signature_update_signature_add_method_nfc,
                setting = DecryptMethodSetting.NFC,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_nfc_accessibility,
                    ).lowercase(),
                testTag = "decryptMethodNFCSetting",
            ),
            DecryptMethodChoiceButtonItem(
                label = R.string.signature_update_signature_add_method_id_card,
                setting = DecryptMethodSetting.ID_CARD,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_id_card_accessibility,
                    ).lowercase(),
                testTag = "decryptMethodIdCardSetting",
            ),
        )
}
