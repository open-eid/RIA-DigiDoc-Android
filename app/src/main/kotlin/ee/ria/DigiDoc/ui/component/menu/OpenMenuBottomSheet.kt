@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding

// Menu component for the bottom sheet (Open document, add more files, sign document and encrypt document)
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OpenMenuBottomSheet(
    modifier: Modifier = Modifier,
    isBottomSheetVisible: MutableState<Boolean> = mutableStateOf(false),
    @StringRes firstButtonStringRes: Int = R.string.main_menu_add_file,
    @StringRes secondButtonStringRes: Int = R.string.main_menu_recent_documents,
    @StringRes firstButtonStringResContentDescription: Int = R.string.main_menu_add_file_accessibility,
    @StringRes secondButtonStringResContentDescription: Int = R.string.main_menu_recent_documents_accessibility,
    @DrawableRes firstButtonIcon: Int = R.drawable.ic_m3_attach_file_48dp_wght400,
    @DrawableRes secondButtonIcon: Int = R.drawable.ic_m3_folder_48dp_wght400,
    firstButtonClick: () -> Unit = {},
    secondButtonClick: () -> Unit = {},
    testTag: String = "menuOpenBottomSheet",
    firstButtonTestTag: String = "menuOpenAddFileButton",
    secondButtonTestTag: String = "menuOpenRecentDocumentsButton",
) {
    if (isBottomSheetVisible.value) {
        ModalBottomSheet(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            onDismissRequest = { isBottomSheetVisible.value = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier =
                    Modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag(testTag)
                        .fillMaxWidth()
                        .padding(SPadding),
            ) {
                TwoButtonMenu(
                    modifier = modifier,
                    firstButtonStringRes = firstButtonStringRes,
                    secondButtonStringRes = secondButtonStringRes,
                    firstButtonStringResContentDescription = firstButtonStringResContentDescription,
                    secondButtonStringResContentDescription = secondButtonStringResContentDescription,
                    firstButtonIcon = firstButtonIcon,
                    secondButtonIcon = secondButtonIcon,
                    firstButtonClick = firstButtonClick,
                    secondButtonClick = secondButtonClick,
                    firstButtonTestTag = firstButtonTestTag,
                    secondButtonTestTag = secondButtonTestTag,
                )
            }
        }
    }
}
