@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.ui.component.shared.BottomSheet
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel

@Composable
fun RecipientBottomSheet(
    modifier: Modifier,
    showSheet: MutableState<Boolean>,
    clickedRecipient: MutableState<Addressee?>,
    sharedRecipientViewModel: SharedRecipientViewModel,
    navController: NavController,
    isRecipientRemoveShown: Boolean = false,
    openRemoveRecipientDialog: MutableState<Boolean>,
    onRecipientRemove: (Addressee?) -> Unit,
) {
    BottomSheet(
        modifier = modifier,
        showSheet = showSheet.value,
        onDismiss = {
            showSheet.value = false
            clickedRecipient.value = null
        },
        buttons =
            listOf(
                BottomSheetButton(
                    icon = R.drawable.ic_m3_expand_content_48dp_wght400,
                    text = stringResource(R.string.recipient_details_title),
                    isExtraActionButtonShown = true,
                    contentDescription = "${stringResource(
                        R.string.recipient_details_title,
                    )} ${formatNumbers(
                        formatName(
                            clickedRecipient.value?.surname,
                            clickedRecipient.value?.givenName,
                            clickedRecipient.value?.identifier,
                        ),
                    )}",
                    onClick = {
                        clickedRecipient.value?.let { recipient ->
                            sharedRecipientViewModel.setRecipient(recipient)
                            navController.navigate(Route.RecipientDetail.route)
                        }
                    },
                ),
                BottomSheetButton(
                    showButton = isRecipientRemoveShown,
                    icon = R.drawable.ic_m3_delete_48dp_wght400,
                    text = stringResource(R.string.recipient_remove_button),
                    contentDescription = "${stringResource(
                        R.string.recipient_remove_button,
                    )} ${formatNumbers(
                        formatName(
                            clickedRecipient.value?.surname,
                            clickedRecipient.value?.givenName,
                            clickedRecipient.value?.identifier,
                        ),
                    )}",
                    onClick = {
                        onRecipientRemove(clickedRecipient.value)
                        openRemoveRecipientDialog.value = true
                    },
                ),
            ),
    )
}
