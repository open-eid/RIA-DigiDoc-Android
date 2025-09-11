@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.bottomsheet

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import ee.ria.DigiDoc.ui.component.shared.handler.containerFileOpeningHandler
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.viewmodel.EncryptViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import java.io.File

@Composable
fun CryptoDataFileBottomSheet(
    modifier: Modifier = Modifier,
    showSheet: Boolean,
    nestedFile: MutableState<File?>,
    onDataFileBottomSheetDismiss: () -> Unit,
    clickedDataFile: MutableState<File?>,
    cryptoContainer: CryptoContainer?,
    sharedContainerViewModel: SharedContainerViewModel,
    encryptViewModel: EncryptViewModel,
    showLoadingScreen: MutableState<Boolean>,
    showSivaDialog: MutableState<Boolean>,
    handleSivaConfirmation: () -> Unit,
    context: Context,
    saveFileLauncher: ActivityResultLauncher<Intent>,
    saveFile: (File?, String, ActivityResultLauncher<Intent>) -> Unit,
    openRemoveFileDialog: MutableState<Boolean>,
    onBackButtonClick: () -> Unit,
) {
    val currentNestedFile = rememberSaveable { mutableStateOf<File?>(null) }

    val buttonName = stringResource(id = R.string.button_name)

    BottomSheet(
        modifier = modifier,
        showSheet = showSheet,
        onDismiss = {
            currentNestedFile.value = null
            onDataFileBottomSheetDismiss()
        },
        buttons =
            listOf(
                BottomSheetButton(
                    icon = R.drawable.ic_m3_expand_content_48dp_wght400,
                    text = stringResource(R.string.main_menu_open_file),
                    contentDescription = "${stringResource(
                        R.string.main_menu_open_file_accessibility,
                    )} ${clickedDataFile.value?.name ?: ""} $buttonName",
                ) {
                    showLoadingScreen.value = true
                    val result =
                        sharedContainerViewModel.openCryptoContainerDataFile(
                            cryptoContainer = cryptoContainer,
                            dataFile = clickedDataFile.value,
                        )
                    containerFileOpeningHandler(
                        result = result,
                        nestedFile = nestedFile,
                        showSivaDialog = showSivaDialog,
                        showLoadingScreen = showLoadingScreen,
                        context = context,
                        signingViewModel = null,
                        encryptViewModel = encryptViewModel,
                        handleSivaConfirmation = handleSivaConfirmation,
                    )
                },
                BottomSheetButton(
                    icon = R.drawable.ic_m3_download_48dp_wght400,
                    text = stringResource(R.string.document_save_button),
                    contentDescription = "${stringResource(
                        R.string.document_save_button,
                    )} ${clickedDataFile.value?.name ?: ""} $buttonName",
                ) {
                    val dataFile = clickedDataFile.value
                    if (dataFile != null) {
                        try {
                            val file = sharedContainerViewModel.getCryptoContainerDataFile(cryptoContainer, dataFile)
                            saveFile(file, dataFile.mimeType(context), saveFileLauncher)
                        } catch (ex: Exception) {
                            errorLog("SigningContainer", "Unable to save file. Unable to get datafile", ex)
                            onBackButtonClick()
                        }
                    }
                },
                BottomSheetButton(
                    showButton = encryptViewModel.isContainerWithoutRecipients(cryptoContainer),
                    icon = R.drawable.ic_m3_delete_48dp_wght400,
                    text = stringResource(R.string.document_remove_button),
                    contentDescription = "${stringResource(
                        R.string.document_remove_button,
                    )} ${clickedDataFile.value?.name ?: ""} $buttonName",
                ) {
                    val dataFile = clickedDataFile.value
                    if (dataFile != null) {
                        openRemoveFileDialog.value = true
                    }
                },
            ),
    )
}
