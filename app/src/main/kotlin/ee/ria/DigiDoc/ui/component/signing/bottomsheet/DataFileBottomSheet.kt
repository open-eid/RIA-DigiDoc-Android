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
import ee.ria.DigiDoc.common.Constant.SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import ee.ria.DigiDoc.utilsLib.extensions.isContainer
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
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
                    text = stringResource(R.string.main_home_open_document_title),
                ) {
                    try {
                        val dataFile = clickedDataFile.value
                        if (dataFile != null) {
                            val containerDataFile =
                                sharedContainerViewModel.getContainerDataFile(
                                    signedContainer,
                                    dataFile,
                                )
                            showLoadingScreen.value = false
                            containerDataFile?.let { file ->
                                if (file.isContainer(context)) {
                                    nestedFile.value = file
                                    currentNestedFile.value = file
                                    val nestedFileMimetype = file.mimeType(context)
                                    if (SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(nestedFileMimetype)) {
                                        showSivaDialog.value = true
                                    } else {
                                        showLoadingScreen.value = true
                                        handleSivaConfirmation()
                                    }
                                } else {
                                    val viewIntent = signingViewModel.getViewIntent(context, file)
                                    context.startActivity(viewIntent, null)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        errorLog("SigningNavigation", "Unable to open container. Unable to get datafiles", ex)
                        onBackButtonClick()
                    }
                },
                BottomSheetButton(
                    icon = R.drawable.ic_m3_download_48dp_wght400,
                    text = stringResource(R.string.document_save_button),
                ) {
                    val dataFile = clickedDataFile.value
                    if (dataFile != null) {
                        try {
                            val file = sharedContainerViewModel.getContainerDataFile(signedContainer, dataFile)
                            saveFile(file, dataFile.mediaType, saveFileLauncher)
                        } catch (ex: Exception) {
                            errorLog("SigningContainer", "Unable to save file. Unable to get datafile", ex)
                            onBackButtonClick()
                        }
                    }
                },
                BottomSheetButton(
                    showButton = signingViewModel.isContainerWithoutSignatures(signedContainer),
                    icon = R.drawable.ic_m3_delete_48dp_wght400,
                    text = stringResource(R.string.document_remove_button),
                ) {
                    val dataFile = clickedDataFile.value
                    if (dataFile != null) {
                        openRemoveFileDialog.value = true
                    }
                },
            ),
    )
}
