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
fun TwoButtonMenu(
    modifier: Modifier = Modifier,
    @StringRes firstButtonStringRes: Int,
    @StringRes secondButtonStringRes: Int,
    @StringRes firstButtonStringResContentDescription: Int,
    @StringRes secondButtonStringResContentDescription: Int,
    @DrawableRes firstButtonIcon: Int,
    @DrawableRes secondButtonIcon: Int,
    firstButtonClick: () -> Unit = {},
    secondButtonClick: () -> Unit = {},
    firstButtonTestTag: String,
    secondButtonTestTag: String,
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
}
