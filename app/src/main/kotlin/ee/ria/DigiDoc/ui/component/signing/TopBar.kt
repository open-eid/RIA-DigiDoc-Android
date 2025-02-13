@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.annotation.StringRes
import androidx.compose.foundation.focusable
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PreventResize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    @StringRes title: Int?,
    leftIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_arrow_back_48dp_wght400),
    @StringRes leftIconContentDescription: Int = R.string.back,
    rightPrimaryIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_help_48dp_wght400),
    @StringRes rightPrimaryIconContentDescription: Int = R.string.main_home_menu_help_accessibility,
    rightSecondaryIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_settings_48dp_wght400),
    @StringRes rightSecondaryIconContentDescription: Int = R.string.main_home_menu_settings_accessibility,
    onLeftButtonClick: () -> Unit = {},
    onRightPrimaryButtonClick: () -> Unit = {},
    onRightSecondaryButtonClick: () -> Unit = {},
) {
    val headingFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var headingTextLoaded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier =
            modifier
                .semantics {
                    isTraversalGroup = true
                    testTagsAsResourceId = true
                }
                .testTag("toolbar"),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        navigationIcon = {
            IconButton(
                modifier = modifier.testTag("toolBarLeftButton"),
                onClick = onLeftButtonClick,
            ) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = stringResource(id = leftIconContentDescription),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        title = {
            if (title != null) {
                PreventResize {
                    Text(
                        text = stringResource(id = title),
                        maxLines = 1,
                        modifier =
                            modifier
                                .semantics { heading() }
                                .focusRequester(headingFocusRequester)
                                .focusable(enabled = true)
                                .focusTarget()
                                .focusProperties { canFocus = true }
                                .onGloballyPositioned {
                                    if (!headingTextLoaded) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            headingFocusRequester.requestFocus()
                                            focusManager.clearFocus()
                                            delay(200)
                                            headingFocusRequester.requestFocus()
                                            headingTextLoaded = true
                                        }
                                    }
                                },
                    )
                }
            } else {
                Text(text = "")
            }
        },
        actions = {
            IconButton(
                modifier = modifier.testTag("toolBarRightPrimaryButton"),
                onClick = onRightPrimaryButtonClick,
            ) {
                Icon(
                    imageVector = rightPrimaryIcon,
                    contentDescription = stringResource(id = rightPrimaryIconContentDescription),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                modifier = modifier.testTag("toolBarRightSecondaryButton"),
                onClick = onRightSecondaryButtonClick,
            ) {
                Icon(
                    imageVector = rightSecondaryIcon,
                    contentDescription = stringResource(id = rightSecondaryIconContentDescription),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}
