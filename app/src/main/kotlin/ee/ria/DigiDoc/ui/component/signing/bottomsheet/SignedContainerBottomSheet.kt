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

package ee.ria.DigiDoc.ui.component.signing.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.SigningViewModel

@Composable
fun SignedContainerBottomSheet(
    modifier: Modifier,
    showSheet: MutableState<Boolean>,
    signedContainer: SignedContainer?,
    isNestedContainer: Boolean,
    isXadesContainer: Boolean,
    isCadesContainer: Boolean,
    signingViewModel: SigningViewModel,
    navController: NavHostController,
    onEncryptClick: () -> Unit,
    onExtendSignatureClick: () -> Unit,
) {
    BottomSheet(
        modifier = modifier,
        showSheet = showSheet.value,
        onDismiss = {
            showSheet.value = false
        },
        buttons =
            listOf(
                BottomSheetButton(
                    showButton =
                        signingViewModel.isSignButtonShown(
                            signedContainer,
                            isNestedContainer,
                            isXadesContainer,
                            isCadesContainer,
                        ),
                    icon = R.drawable.ic_m3_stylus_note_48dp_wght400,
                    text = stringResource(R.string.add_signature_button),
                    isExtraActionButtonShown = true,
                    onClick = {
                        navController.navigate(
                            Route.SignatureInputScreen.route,
                        )
                    },
                ),
                BottomSheetButton(
                    showButton = signingViewModel.isEncryptButtonShown(signedContainer, isNestedContainer),
                    icon = R.drawable.ic_m3_encrypted_48dp_wght400,
                    text = stringResource(R.string.main_menu_encrypt_container),
                    isExtraActionButtonShown = true,
                    onClick = onEncryptClick,
                ),
                BottomSheetButton(
                    showButton = !isNestedContainer,
                    icon = R.drawable.ic_m3_more_time_48dp_wght400,
                    text = stringResource(R.string.extend_signature_button),
                    isExtraActionButtonShown = true,
                    onClick = onExtendSignatureClick,
                ),
            ),
    )
}
