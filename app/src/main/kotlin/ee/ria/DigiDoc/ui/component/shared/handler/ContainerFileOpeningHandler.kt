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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.shared.handler

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.ContainerFileOpeningResult
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.EncryptViewModel
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import java.io.File

fun containerFileOpeningHandler(
    result: ContainerFileOpeningResult,
    nestedFile: MutableState<File?>,
    showSivaDialog: MutableState<Boolean>,
    showLoadingScreen: MutableState<Boolean>,
    context: Context,
    signingViewModel: SigningViewModel?,
    encryptViewModel: EncryptViewModel?,
    handleSivaConfirmation: () -> Unit,
) {
    when (result) {
        is ContainerFileOpeningResult.OpenNestedFile -> {
            nestedFile.value = result.file
            if (result.needsSivaDialog) {
                showSivaDialog.value = true
            } else {
                showLoadingScreen.value = true
                handleSivaConfirmation()
            }
        }
        is ContainerFileOpeningResult.OpenWithFile -> {
            var intent: Intent? = null
            if (signingViewModel != null) {
                intent = signingViewModel.getViewIntent(context, result.file)
            } else if (encryptViewModel != null) {
                intent = encryptViewModel.getViewIntent(context, result.file)
            }
            context.startActivity(intent, null)
        }
        is ContainerFileOpeningResult.Error -> {
            showMessage(context, R.string.container_open_file_error)
        }
    }
    showLoadingScreen.value = false
}
