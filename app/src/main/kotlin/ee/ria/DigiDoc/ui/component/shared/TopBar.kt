/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
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
import androidx.core.net.toUri
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
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
    @DrawableRes extraButtonIcon: Int = R.drawable.ic_m3_notifications_48dp_wght400,
    @StringRes extraButtonIconContentDescription: Int = R.string.notifications,
    showRightSideIcons: Boolean = true,
    showNavigationIcon: Boolean = true,
    onLeftButtonClick: () -> Unit = {},
    onRightPrimaryButtonClick: (() -> Unit)? = null,
    onRightSecondaryButtonClick: () -> Unit = {},
    onExtraButtonClick: () -> Unit = {},
    showExtraButton: Boolean = false,
    extraButtonItemCount: Int = 0,
    sharedMenuViewModel: SharedMenuViewModel,
) {
    val context = LocalContext.current
    var onRightPrimaryButtonClick = onRightPrimaryButtonClick
    if (onRightPrimaryButtonClick == null) {
        @Suppress("AssignedValueIsNeverRead")
        onRightPrimaryButtonClick = {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    context.getString(R.string.main_home_menu_help_url).toUri(),
                )

            context.startActivity(browserIntent, null)
        }
    }

    val headingFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var headingTextLoaded by remember { mutableStateOf(false) }

    val isEstonianLanguageUsed = remember { mutableStateOf(false) }
    val isTtsInitialized by sharedMenuViewModel.isTtsInitialized.asFlow().collectAsState(false)

    LaunchedEffect(isTtsInitialized) {
        if (isTtsInitialized) {
            isEstonianLanguageUsed.value = sharedMenuViewModel.isEstonianLanguageUsed()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    TopAppBar(
        modifier =
            modifier
                .semantics {
                    isTraversalGroup = true
                    testTagsAsResourceId = true
                }.testTag("toolbar"),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        navigationIcon = {
            if (showNavigationIcon) {
                IconButton(
                    modifier = modifier.testTag("toolBarLeftButton"),
                    onClick = {
                        // Add debounce to prevent rapid navigation clicks
                        debounceJob?.cancel()
                        debounceJob =
                            coroutineScope.launch {
                                onLeftButtonClick()
                            }
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = leftIcon),
                        contentDescription = stringResource(id = leftIconContentDescription),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .focusable(false)
                                .testTag("leftNavigationButton"),
                    )
                }
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
                                        CoroutineScope(Main).launch {
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
            if (showRightSideIcons) {
                if (showExtraButton) {
                    IconButton(
                        modifier = modifier.testTag("toolBarExtraButton"),
                        onClick = {
                            // Add debounce to prevent rapid navigation clicks
                            debounceJob?.cancel()
                            debounceJob =
                                coroutineScope.launch {
                                    onExtraButtonClick()
                                }
                        },
                    ) {
                        BadgedBox(
                            badge = {
                                if (extraButtonItemCount > 0) {
                                    Badge(
                                        containerColor = Red500,
                                        contentColor = Color.White,
                                    ) {
                                        Text("$extraButtonItemCount")
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = extraButtonIcon),
                                contentDescription = stringResource(extraButtonIconContentDescription),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier =
                                    modifier
                                        .size(iconSizeXXS)
                                        .focusable(false)
                                        .testTag("extraNavigationButton"),
                            )
                        }
                    }
                }
                IconButton(
                    modifier = modifier.testTag("toolBarRightPrimaryButton"),
                    onClick = {
                        // Add debounce to prevent rapid navigation clicks
                        debounceJob?.cancel()
                        debounceJob =
                            coroutineScope.launch {
                                onRightPrimaryButtonClick()
                            }
                    },
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
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .focusable(false)
                                .testTag("rightPrimaryNavigationButton"),
                    )
                }
                IconButton(
                    modifier = modifier.testTag("toolBarRightSecondaryButton"),
                    onClick = {
                        // Add debounce to prevent rapid navigation clicks
                        debounceJob?.cancel()
                        debounceJob =
                            coroutineScope.launch {
                                onRightSecondaryButtonClick()
                            }
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = rightSecondaryIcon),
                        contentDescription = stringResource(id = rightSecondaryIconContentDescription),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .focusable(false)
                                .testTag("rightSecondaryNavigationButton"),
                    )
                }
            }
        },
    )
}
