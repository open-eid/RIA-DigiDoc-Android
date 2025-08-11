@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.NO_REMOVE_SIGNATURE_BUTTON_FILE_MIMETYPES
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel
import org.apache.commons.io.FilenameUtils

@Composable
fun SignatureBottomSheet(
    modifier: Modifier,
    showSheet: MutableState<Boolean>,
    clickedSignature: MutableState<SignatureInterface?>,
    isTimestamp: Boolean,
    signedContainer: SignedContainer?,
    signingViewModel: SigningViewModel,
    sharedSignatureViewModel: SharedSignatureViewModel,
    navController: NavController,
    isNestedContainer: Boolean,
    isXadesContainer: Boolean,
    isCadesContainer: Boolean,
    openRemoveSignatureDialog: MutableState<Boolean>,
    onSignatureRemove: (SignatureInterface?) -> Unit,
) {
    BottomSheet(
        modifier = modifier,
        showSheet = showSheet.value,
        onDismiss = {
            showSheet.value = false
            clickedSignature.value = null
        },
        buttons =
            listOf(
                BottomSheetButton(
                    icon = R.drawable.ic_m3_database_48dp_wght400,
                    text = stringResource(R.string.signature_details_title),
                    isExtraActionButtonShown = true,
                    contentDescription = "${stringResource(
                        R.string.signature_details_title,
                    )} ${formatNumbers(formatName(clickedSignature.value?.signedBy ?: ""))}",
                    onClick = {
                        clickedSignature.value?.let { signature ->
                            sharedSignatureViewModel.setSignature(signature)
                            sharedSignatureViewModel.setIsTimestamp(isTimestamp)
                            navController.navigate(Route.SignerDetail.route)
                        }
                    },
                ),
                BottomSheetButton(
                    showButton =
                        !NO_REMOVE_SIGNATURE_BUTTON_FILE_MIMETYPES.contains(
                            signedContainer
                                ?.getContainerFile()
                                ?.let { containerFile ->
                                    if (containerFile.exists()) {
                                        signingViewModel.getMimetype(containerFile)
                                    } else {
                                        ""
                                    }
                                },
                        ) &&
                            !NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS.contains(
                                FilenameUtils.getExtension(signedContainer?.getName()),
                            ) &&
                            !isNestedContainer &&
                            !isXadesContainer &&
                            !isCadesContainer,
                    icon = R.drawable.ic_m3_delete_48dp_wght400,
                    text = stringResource(R.string.signature_remove_button),
                    contentDescription = "${stringResource(
                        R.string.signature_remove_button,
                    )} ${formatNumbers(formatName(clickedSignature.value?.signedBy ?: ""))}",
                    onClick = {
                        onSignatureRemove(clickedSignature.value)
                        openRemoveSignatureDialog.value = true
                    },
                ),
            ),
    )
}
