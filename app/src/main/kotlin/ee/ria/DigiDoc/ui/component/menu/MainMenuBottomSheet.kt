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
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.utils.Route

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainMenuBottomSheet(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isBottomSheetVisible: MutableState<Boolean> = mutableStateOf(false),
    @StringRes firstButtonStringRes: Int = R.string.main_home_menu_about,
    @StringRes secondButtonStringRes: Int = R.string.main_home_menu_accessibility,
    @StringRes thirdButtonStringRes: Int = R.string.main_home_menu_diagnostics,
    @StringRes firstButtonStringResContentDescription: Int = R.string.main_home_menu_about_accessibility,
    @StringRes secondButtonStringResContentDescription: Int = R.string.main_home_menu_accessibility_accessibility,
    @StringRes thirdButtonStringResContentDescription: Int = R.string.main_home_menu_diagnostics_accessibility,
    @DrawableRes firstButtonIcon: Int = R.drawable.ic_m3_info_48dp_wght400,
    @DrawableRes secondButtonIcon: Int = R.drawable.ic_m3_accessibility_new_48dp_wght400,
    @DrawableRes thirdButtonIcon: Int = R.drawable.ic_m3_show_chart_48dp_wght400,
    firstButtonClick: (() -> Unit)? = null,
    secondButtonClick: (() -> Unit)? = null,
    thirdButtonClick: (() -> Unit)? = null,
    testTag: String = "menuMainBottomSheet",
    firstButtonTestTag: String = "menuFileOpenFileButton",
    secondButtonTestTag: String = "menuFileSaveFileButton",
    thirdButtonTestTag: String = "menuFileRemoveFileButton",
) {
    if (isBottomSheetVisible.value) {
        val firstButtonClick =
            firstButtonClick ?: {
                isBottomSheetVisible.value = false
                navController.navigate(
                    Route.Info.route,
                )
            }
        val secondButtonClick =
            secondButtonClick ?: {
                isBottomSheetVisible.value = false
                navController.navigate(
                    Route.Accessibility.route,
                )
            }
        val thirdButtonClick =
            thirdButtonClick ?: {
                isBottomSheetVisible.value = false
                navController.navigate(
                    Route.Diagnostics.route,
                )
            }
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
                        }.testTag(testTag)
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
