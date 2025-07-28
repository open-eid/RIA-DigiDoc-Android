@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.myeid.MyEidIdentificationMethodSetting

data class MyEidIdentificationMethodChoiceButtonItem(
    @param:StringRes val label: Int = 0,
    val setting: MyEidIdentificationMethodSetting = MyEidIdentificationMethodSetting.NFC,
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun radioItems(): List<MyEidIdentificationMethodChoiceButtonItem> =
        listOf(
            MyEidIdentificationMethodChoiceButtonItem(
                label = R.string.signature_update_signature_add_method_nfc,
                setting = MyEidIdentificationMethodSetting.NFC,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_nfc_accessibility,
                    ).lowercase(),
                testTag = "identificationMethodNFCSetting",
            ),
            MyEidIdentificationMethodChoiceButtonItem(
                label = R.string.signature_update_signature_add_method_id_card,
                setting = MyEidIdentificationMethodSetting.ID_CARD,
                contentDescription =
                    stringResource(
                        id = R.string.signature_update_signature_add_method_id_card_accessibility,
                    ).lowercase(),
                testTag = "identificationMethodIdCardSetting",
            ),
        )
}
