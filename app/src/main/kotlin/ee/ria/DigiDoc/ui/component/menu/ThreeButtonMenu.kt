@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThreeButtonMenu(
    modifier: Modifier = Modifier,
    @StringRes firstButtonStringRes: Int,
    @StringRes secondButtonStringRes: Int,
    @StringRes thirdButtonStringRes: Int,
    @StringRes firstButtonStringResContentDescription: Int,
    @StringRes secondButtonStringResContentDescription: Int,
    @StringRes thirdButtonStringResContentDescription: Int,
    @DrawableRes firstButtonIcon: Int,
    @DrawableRes secondButtonIcon: Int,
    @DrawableRes thirdButtonIcon: Int,
    firstButtonClick: () -> Unit = {},
    secondButtonClick: () -> Unit = {},
    thirdButtonClick: () -> Unit = {},
    firstButtonTestTag: String,
    secondButtonTestTag: String,
    thirdButtonTestTag: String,
) {
    MenuButton(
        modifier = modifier,
        buttonStringRes = firstButtonStringRes,
        buttonIcon = firstButtonIcon,
        buttonStringResContentDescription = firstButtonStringResContentDescription,
        buttonClick = firstButtonClick,
        testTag = firstButtonTestTag,
    )
    MenuButton(
        modifier = modifier,
        buttonStringRes = secondButtonStringRes,
        buttonIcon = secondButtonIcon,
        buttonStringResContentDescription = secondButtonStringResContentDescription,
        buttonClick = secondButtonClick,
        testTag = secondButtonTestTag,
    )
    MenuButton(
        modifier = modifier,
        buttonStringRes = thirdButtonStringRes,
        buttonIcon = thirdButtonIcon,
        buttonStringResContentDescription = thirdButtonStringResContentDescription,
        buttonClick = thirdButtonClick,
        testTag = thirdButtonTestTag,
    )
}
