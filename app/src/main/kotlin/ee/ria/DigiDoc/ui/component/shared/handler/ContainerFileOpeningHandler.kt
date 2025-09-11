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
