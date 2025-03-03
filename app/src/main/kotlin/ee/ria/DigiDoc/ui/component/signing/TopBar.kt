@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PreventResize
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import ee.ria.DigiDoc.viewmodel.MenuViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    @StringRes title: Int?,
    @DrawableRes leftIcon: Int = R.drawable.ic_m3_arrow_back_48dp_wght400,
    @StringRes leftIconContentDescription: Int = R.string.back,
    @DrawableRes rightPrimaryIcon: Int = R.drawable.ic_m3_help_48dp_wght400,
    @StringRes rightPrimaryIconContentDescription: Int = R.string.main_home_menu_help_accessibility,
    @DrawableRes rightSecondaryIcon: Int = R.drawable.ic_m3_settings_48dp_wght400,
    @StringRes rightSecondaryIconContentDescription: Int = R.string.main_home_menu_settings_accessibility,
    onLeftButtonClick: () -> Unit = {},
    onRightPrimaryButtonClick: (() -> Unit)? = null,
    onRightSecondaryButtonClick: () -> Unit = {},
    menuViewModel: MenuViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var onRightPrimaryButtonClick = onRightPrimaryButtonClick
    if (onRightPrimaryButtonClick == null) {
        onRightPrimaryButtonClick = {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.main_home_menu_help_url)),
                )

            context.startActivity(browserIntent, null)
        }
    }

    val headingFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var headingTextLoaded by remember { mutableStateOf(false) }

    val isEstonianLanguageUsed = remember { mutableStateOf(false) }
    val isTtsInitialized by menuViewModel.isTtsInitialized.asFlow().collectAsState(false)

    LaunchedEffect(isTtsInitialized) {
        if (isTtsInitialized) {
            isEstonianLanguageUsed.value = menuViewModel.isEstonianLanguageUsed()
        }
    }

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
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        navigationIcon = {
            IconButton(
                modifier = modifier.testTag("toolBarLeftButton"),
                onClick = onLeftButtonClick,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = leftIcon),
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
                        maxLines = 2,
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
                    imageVector = ImageVector.vectorResource(id = rightPrimaryIcon),
                    contentDescription =
                        if (rightPrimaryIconContentDescription == R.string.main_home_menu_help_accessibility) {
                            if (isEstonianLanguageUsed.value) {
                                stringResource(id = R.string.main_home_menu_help) +
                                    " link " +
                                    "w w w punkt i d punkt e e"
                            } else {
                                stringResource(id = R.string.main_home_menu_help) + " " +
                                    TextUtil.splitTextAndJoin(
                                        stringResource(id = R.string.main_home_menu_help_url_short),
                                        "",
                                        " ",
                                    )
                            }
                        } else {
                            stringResource(id = rightPrimaryIconContentDescription)
                        },
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                modifier = modifier.testTag("toolBarRightSecondaryButton"),
                onClick = onRightSecondaryButtonClick,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = rightSecondaryIcon),
                    contentDescription = stringResource(id = rightSecondaryIconContentDescription),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}
