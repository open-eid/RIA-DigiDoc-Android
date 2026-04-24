/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
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

package ee.ria.DigiDoc.fragment.screen

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.theme.ThemeSetting
import ee.ria.DigiDoc.ui.component.info.InfoComponent
import ee.ria.DigiDoc.ui.component.info.InfoComponentItem
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.shared.keyboard.keyboardScrollable
import ee.ria.DigiDoc.ui.theme.Dimensions.MCornerRadius
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXL
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    val context = LocalContext.current

    val screenScrollState = rememberScrollState()

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val isEstonianLanguageUsed = remember { mutableStateOf(false) }
    val isTtsInitialized by sharedMenuViewModel.isTtsInitialized.asFlow().collectAsState(false)

    val themeSetting = remember { sharedSettingsViewModel.dataStore.getThemeSetting() }
    val isDarkTheme =
        when (themeSetting) {
            ThemeSetting.DARK -> true
            ThemeSetting.LIGHT -> false
            ThemeSetting.SYSTEM -> isSystemInDarkTheme()
        }
    val cofundedLogo =
        ImageBitmap.imageResource(
            if (isDarkTheme) R.drawable.cofunded_eu else R.drawable.cofunded_eu_tp,
        )
    // Remove border from image
    val cofundedLogoWidthPx = cofundedLogo.width - XSPadding.value.toInt()
    val cofundedLogoBlendMode = if (isDarkTheme) BlendMode.Lighten else BlendMode.SrcOver
    val logoSurfaceColor = MaterialTheme.colorScheme.surface
    val logoDescription = stringResource(R.string.main_about_digidoc_and_el_logos)

    LaunchedEffect(isTtsInitialized) {
        if (isTtsInitialized) {
            isEstonianLanguageUsed.value = sharedMenuViewModel.isEstonianLanguageUsed()
        }
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("infoScreen"),
        snackbarHost = { StatusSnackbarHost() },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_home_menu_about,
                onLeftButtonClick = {
                    navController.navigateUp()
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    modifier
                        .verticalScroll(screenScrollState)
                        .keyboardScrollable(screenScrollState)
                        .fillMaxWidth()
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag("scrollView"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier =
                        Modifier
                            .padding(horizontal = XSPadding, vertical = SPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(0.4f)
                                .widthIn(max = iconSizeXXL)
                                .padding(end = XSPadding)
                                .aspectRatio(cofundedLogoWidthPx.toFloat() / cofundedLogo.height)
                                .semantics {
                                    contentDescription = logoDescription
                                    stateDescription = "logo"
                                    role = Role.Image
                                }.drawBehind {
                                    drawRect(logoSurfaceColor)
                                    drawImage(
                                        image = cofundedLogo,
                                        srcSize = IntSize(cofundedLogoWidthPx, cofundedLogo.height),
                                        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                                        blendMode = cofundedLogoBlendMode,
                                    )
                                },
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Column(
                            modifier = modifier.semantics(mergeDescendants = true) {},
                        ) {
                            Text(
                                text = stringResource(id = R.string.main_about_ria_digidoc_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text =
                                    String.format(
                                        stringResource(id = R.string.main_about_version_title),
                                        BuildConfig.VERSION_NAME + '.' + BuildConfig.VERSION_CODE,
                                    ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Text(
                            modifier = Modifier.padding(vertical = SPadding),
                            text = stringResource(id = R.string.main_about_info_title),
                            style = MaterialTheme.typography.labelMedium,
                        )

                        val helpButtonContentDescriptionText =
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
                        ElevatedButton(
                            modifier =
                                Modifier
                                    .shadow(
                                        elevation = MCornerRadius,
                                        shape = buttonRoundCornerShape,
                                        ambientColor = MaterialTheme.colorScheme.primary,
                                        spotColor = MaterialTheme.colorScheme.primary,
                                    ).clip(buttonRoundCornerShape)
                                    .semantics {
                                        contentDescription = helpButtonContentDescriptionText
                                        stateDescription = "link"
                                        testTagsAsResourceId = true
                                    }.testTag("mainInfoHelpButton"),
                            colors =
                                ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            shape = buttonRoundCornerShape,
                            contentPadding =
                                PaddingValues(
                                    vertical = XSPadding,
                                    horizontal = SPadding,
                                ),
                            onClick = {
                                val browserIntent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        context.getString(R.string.main_home_menu_help_url).toUri(),
                                    )

                                context.startActivity(browserIntent, null)
                            },
                        ) {
                            Icon(
                                modifier = Modifier.size(iconSizeXXS),
                                imageVector =
                                    ImageVector.vectorResource(
                                        id = R.drawable.ic_m3_open_in_new_48dp_wght400,
                                    ),
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(XSPadding))
                            Text(
                                modifier = Modifier.notAccessible(),
                                text = stringResource(id = R.string.main_about_help_center),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                            )
                        }
                    }
                }
                Text(
                    modifier =
                        modifier
                            .padding(
                                start = SPadding,
                                top = MPadding,
                                end = SPadding,
                                bottom = SPadding,
                            ).semantics { heading() },
                    text =
                        String.format(
                            stringResource(id = R.string.main_about_licenses_title),
                            BuildConfig.VERSION_NAME + '.' + BuildConfig.VERSION_CODE,
                        ),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge,
                )
                InfoComponentItem().componentItems().forEachIndexed { _, componentItem ->
                    InfoComponent(
                        modifier = modifier,
                        name = componentItem.name,
                        licenseName = componentItem.licenseName,
                        licenseUrl = componentItem.licenseUrl,
                    )
                }
                InvisibleElement(modifier = modifier)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InfoScreenPreview() {
    RIADigiDocTheme {
        InfoScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedSettingsViewModel = hiltViewModel(),
        )
    }
}
