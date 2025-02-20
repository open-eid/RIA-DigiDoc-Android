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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileMenuBottomSheet(
    modifier: Modifier = Modifier,
    isBottomSheetVisible: MutableState<Boolean> = mutableStateOf(false),
    @StringRes firstButtonStringRes: Int = R.string.main_menu_open_file,
    @StringRes secondButtonStringRes: Int = R.string.main_menu_save_file,
    @StringRes thirdButtonStringRes: Int = R.string.main_menu_remove_file,
    @StringRes firstButtonStringResContentDescription: Int = R.string.main_menu_open_file_accessibility,
    @StringRes secondButtonStringResContentDescription: Int = R.string.main_menu_save_file_accessibility,
    @StringRes thirdButtonStringResContentDescription: Int = R.string.main_menu_remove_file_accessibility,
    @DrawableRes firstButtonIcon: Int = R.drawable.ic_m3_expand_content_48dp_wght400,
    @DrawableRes secondButtonIcon: Int = R.drawable.ic_m3_download_48dp_wght400,
    @DrawableRes thirdButtonIcon: Int = R.drawable.ic_m3_delete_48dp_wght400,
    firstButtonClick: () -> Unit = {},
    secondButtonClick: () -> Unit = {},
    thirdButtonClick: () -> Unit = {},
    testTag: String = "menuFileBottomSheet",
    firstButtonTestTag: String = "menuFileOpenFileButton",
    secondButtonTestTag: String = "menuFileSaveFileButton",
    thirdButtonTestTag: String = "menuFileRemoveFileButton",
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
                ThreeButtonMenu(
                    modifier = modifier,
                    firstButtonStringRes = firstButtonStringRes,
                    secondButtonStringRes = secondButtonStringRes,
                    thirdButtonStringRes = thirdButtonStringRes,
                    firstButtonStringResContentDescription = firstButtonStringResContentDescription,
                    secondButtonStringResContentDescription = secondButtonStringResContentDescription,
                    thirdButtonStringResContentDescription = thirdButtonStringResContentDescription,
                    firstButtonIcon = firstButtonIcon,
                    secondButtonIcon = secondButtonIcon,
                    thirdButtonIcon = thirdButtonIcon,
                    firstButtonClick = firstButtonClick,
                    secondButtonClick = secondButtonClick,
                    thirdButtonClick = thirdButtonClick,
                    firstButtonTestTag = firstButtonTestTag,
                    secondButtonTestTag = secondButtonTestTag,
                    thirdButtonTestTag = thirdButtonTestTag,
                )
            }
        }
    }
}
