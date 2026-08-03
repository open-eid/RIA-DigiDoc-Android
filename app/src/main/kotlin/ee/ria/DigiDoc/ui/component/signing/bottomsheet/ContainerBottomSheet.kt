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

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import java.io.File

@Composable
fun ContainerBottomSheet(
    modifier: Modifier,
    showSheet: MutableState<Boolean>,
    isSaveButtonShown: Boolean = false,
    isEditContainerButtonShown: Boolean = true,
    openEditContainerNameDialog: MutableState<Boolean>,
    isEncryptButtonShown: Boolean = true,
    signedContainer: SignedContainer?,
    onEncryptClick: () -> Unit,
    saveFileLauncher: ActivityResultLauncher<Intent>,
    saveFile: (File, String?, ActivityResultLauncher<Intent>) -> Unit,
) {
    val buttonName = stringResource(id = R.string.button_name)

    BottomSheet(
        modifier = modifier,
        showSheet = showSheet.value,
        onDismiss = {
            showSheet.value = false
        },
        buttons =
            listOf(
                BottomSheetButton(
                    showButton = isEditContainerButtonShown,
                    icon = R.drawable.ic_m3_edit_48dp_wght400,
                    text = stringResource(R.string.signing_container_name_update_button),
                    contentDescription = "${stringResource(
                        R.string.signing_container_name_update_button,
                    )} ${signedContainer?.getName() ?: ""} $buttonName",
                ) {
                    openEditContainerNameDialog.value = true
                },
                BottomSheetButton(
                    showButton = isSaveButtonShown,
                    icon = R.drawable.ic_m3_download_48dp_wght400,
                    text = stringResource(R.string.container_save),
                    contentDescription = "${stringResource(
                        R.string.container_save,
                    )} ${signedContainer?.getName() ?: ""} $buttonName",
                ) {
                    val file = signedContainer?.getContainerFile()
                    if (file != null) {
                        saveFile(
                            file,
                            signedContainer.containerMimetype(),
                            saveFileLauncher,
                        )
                    }
                },
                BottomSheetButton(
                    showButton = isEncryptButtonShown,
                    icon = R.drawable.ic_m3_encrypted_48dp_wght400,
                    text = stringResource(R.string.encrypt_button),
                    contentDescription = "${stringResource(
                        R.string.encrypt_button,
                    )} ${signedContainer?.getName() ?: ""} $buttonName",
                    isExtraActionButtonShown = true,
                    onClick = onEncryptClick,
                ),
            ),
    )
}
