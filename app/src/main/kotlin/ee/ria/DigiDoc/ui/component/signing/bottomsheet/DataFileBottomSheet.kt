@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.bottomsheet

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
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import ee.ria.DigiDoc.ui.component.shared.handler.containerFileOpeningHandler
import ee.ria.DigiDoc.utilsLib.extensions.isCryptoContainer
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import java.io.File

@Composable
fun DataFileBottomSheet(
    modifier: Modifier = Modifier,
    showSheet: Boolean,
    nestedFile: MutableState<File?>,
    onDataFileBottomSheetDismiss: () -> Unit,
    clickedDataFile: MutableState<DataFileInterface?>,
    signedContainer: SignedContainer?,
    sharedContainerViewModel: SharedContainerViewModel,
    signingViewModel: SigningViewModel,
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
                    )} ${clickedDataFile.value?.fileName ?: ""} $buttonName",
                ) {
                    showLoadingScreen.value = true
                    val result =
                        sharedContainerViewModel.openContainerDataFile(
                            signedContainer = signedContainer,
                            clickedDataFile = clickedDataFile.value,
                            context = context,
                        )
                    containerFileOpeningHandler(
                        result = result,
                        nestedFile = nestedFile,
                        currentNestedFile = currentNestedFile,
                        showSivaDialog = showSivaDialog,
                        showLoadingScreen = showLoadingScreen,
                        context = context,
                        signingViewModel = signingViewModel,
                        handleSivaConfirmation = handleSivaConfirmation,
                    )
                },
                BottomSheetButton(
                    icon = R.drawable.ic_m3_download_48dp_wght400,
                    text = stringResource(R.string.document_save_button),
                    contentDescription = "${stringResource(
                        R.string.document_save_button,
                    )} ${clickedDataFile.value?.fileName ?: ""} $buttonName",
                ) {
                    val dataFile = clickedDataFile.value

                    dataFile?.let {
                        try {
                            val file = sharedContainerViewModel.getContainerDataFile(signedContainer, it)

                            val mimeType =
                                when {
                                    file?.isCryptoContainer() == true -> Constant.CONTAINER_MIME_TYPE
                                    else -> it.mediaType
                                }

                            saveFile(file, mimeType, saveFileLauncher)
                        } catch (ex: Exception) {
                            errorLog("DataFileBottomSheet", "Unable to save file. Unable to get data file", ex)
                            onBackButtonClick()
                        }
                    } ?: run {
                        errorLog("DataFileBottomSheet", "Data file is null")
                    }
                },
                BottomSheetButton(
                    showButton =
                        signingViewModel.isContainerWithoutSignatures(signedContainer) &&
                            !sharedContainerViewModel.isNestedContainer(signedContainer),
                    icon = R.drawable.ic_m3_delete_48dp_wght400,
                    text = stringResource(R.string.document_remove_button),
                    contentDescription = "${stringResource(
                        R.string.document_remove_button,
                    )} ${clickedDataFile.value?.fileName ?: ""} $buttonName",
                ) {
                    val dataFile = clickedDataFile.value
                    if (dataFile != null) {
                        openRemoveFileDialog.value = true
                    }
                },
            ),
    )
}
