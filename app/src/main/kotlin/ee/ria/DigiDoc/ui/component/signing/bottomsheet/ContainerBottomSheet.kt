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
    isEditContainerButtonShown: Boolean = true,
    openEditContainerNameDialog: MutableState<Boolean>,
    isEncryptButtonShown: Boolean = true,
    signedContainer: SignedContainer?,
    saveFileLauncher: ActivityResultLauncher<Intent>,
    saveFile: (File?, String?, ActivityResultLauncher<Intent>) -> Unit,
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
                    icon = R.drawable.ic_m3_download_48dp_wght400,
                    text = stringResource(R.string.container_save),
                    contentDescription = "${stringResource(
                        R.string.container_save,
                    )} ${signedContainer?.getName() ?: ""} $buttonName",
                ) {
                    saveFile(
                        signedContainer?.getContainerFile(),
                        signedContainer?.containerMimetype(),
                        saveFileLauncher,
                    )
                },
                BottomSheetButton(
                    showButton = isEncryptButtonShown,
                    icon = R.drawable.ic_m3_encrypted_48dp_wght400,
                    text = stringResource(R.string.crypto_button),
                    contentDescription = "${stringResource(
                        R.string.crypto_button,
                    )} ${signedContainer?.getName() ?: ""} $buttonName",
                    isExtraActionButtonShown = true,
                ) {
                    // TODO: Implement encrypt click
                },
            ),
    )
}
