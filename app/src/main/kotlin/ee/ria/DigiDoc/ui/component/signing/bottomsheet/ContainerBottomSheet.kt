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
    openEditContainerNameDialog: MutableState<Boolean>,
    signedContainer: SignedContainer?,
    saveFileLauncher: ActivityResultLauncher<Intent>,
    saveFile: (File?, String?, ActivityResultLauncher<Intent>) -> Unit,
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
                    icon = R.drawable.ic_m3_edit_48dp_wght400,
                    text = stringResource(R.string.signing_container_name_update_button),
                ) {
                    openEditContainerNameDialog.value = true
                },
                BottomSheetButton(
                    icon = R.drawable.ic_m3_download_48dp_wght400,
                    text = stringResource(R.string.container_save),
                ) {
                    saveFile(
                        signedContainer?.getContainerFile(),
                        signedContainer?.containerMimetype(),
                        saveFileLauncher,
                    )
                },
                BottomSheetButton(
                    icon = R.drawable.ic_m3_encrypted_48dp_wght400,
                    text = stringResource(R.string.crypto_button),
                    isExtraActionButtonShown = true,
                ) {
                    // TODO: Implement encrypt click
                },
            ),
    )
}
