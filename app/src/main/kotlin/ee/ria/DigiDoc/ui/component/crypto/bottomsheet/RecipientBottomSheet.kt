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

package ee.ria.DigiDoc.ui.component.crypto.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.CertType
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel

@Composable
fun RecipientBottomSheet(
    modifier: Modifier,
    showSheet: MutableState<Boolean>,
    clickedRecipient: MutableState<Addressee?>,
    sharedRecipientViewModel: SharedRecipientViewModel,
    navController: NavController,
    isRecipientRemoveShown: Boolean = false,
    isDecryptShown: Boolean = false,
    onDecrypt: (Addressee) -> Unit = {},
    openRemoveRecipientDialog: MutableState<Boolean>,
    onRecipientRemove: (Addressee?) -> Unit,
) {
    val clickedPasswordRecipient =
        clickedRecipient.value?.takeIf { it.certType == CertType.PasswordType }

    BottomSheet(
        modifier = modifier,
        showSheet = showSheet.value,
        onDismiss = {
            showSheet.value = false
            clickedRecipient.value = null
        },
        buttons =
            listOf(
                BottomSheetButton(
                    showButton = isDecryptShown && clickedPasswordRecipient != null,
                    icon = R.drawable.ic_m3_encrypted_off_48dp_wght400,
                    text = stringResource(R.string.decrypt_button),
                    contentDescription = "${stringResource(
                        R.string.decrypt_button,
                    )} ${formatNumbers(clickedPasswordRecipient?.identifier ?: "")}",
                    onClick = {
                        clickedPasswordRecipient?.let(onDecrypt)
                    },
                ),
                BottomSheetButton(
                    icon = R.drawable.ic_m3_expand_content_48dp_wght400,
                    text = stringResource(R.string.recipient_details_title),
                    isExtraActionButtonShown = true,
                    contentDescription = "${stringResource(
                        R.string.recipient_details_title,
                    )} ${formatNumbers(
                        formatName(
                            clickedRecipient.value?.surname,
                            clickedRecipient.value?.givenName,
                            clickedRecipient.value?.identifier,
                        ),
                    )}",
                    onClick = {
                        clickedRecipient.value?.let { recipient ->
                            sharedRecipientViewModel.setRecipient(recipient)
                            navController.navigate(Route.RecipientDetail.route)
                        }
                    },
                ),
                BottomSheetButton(
                    showButton = isRecipientRemoveShown,
                    icon = R.drawable.ic_m3_delete_48dp_wght400,
                    text = stringResource(R.string.recipient_remove_button),
                    contentDescription = "${stringResource(
                        R.string.recipient_remove_button,
                    )} ${formatNumbers(
                        formatName(
                            clickedRecipient.value?.surname,
                            clickedRecipient.value?.givenName,
                            clickedRecipient.value?.identifier,
                        ),
                    )}",
                    onClick = {
                        onRecipientRemove(clickedRecipient.value)
                        openRemoveRecipientDialog.value = true
                    },
                ),
            ),
    )
}
