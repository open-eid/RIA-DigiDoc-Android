/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
