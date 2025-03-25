@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.info.InfoComponent
import ee.ria.DigiDoc.ui.component.info.InfoComponentItem
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MCornerRadius
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val isEstonianLanguageUsed = remember { mutableStateOf(false) }
    val isTtsInitialized by sharedMenuViewModel.isTtsInitialized.asFlow().collectAsState(false)

    LaunchedEffect(isTtsInitialized) {
        if (isTtsInitialized) {
            isEstonianLanguageUsed.value = sharedMenuViewModel.isEstonianLanguageUsed()
        }
    }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                modifier = modifier.padding(vertical = SPadding),
                hostState = snackBarHostState,
            )
        },
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("infoScreen"),
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
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("scrollView"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier =
                        modifier
                            .padding(horizontal = XSPadding, vertical = SPadding),
                ) {
                    Column(
                        modifier =
                            modifier
                                .padding(end = XSPadding)
                                .weight(.35f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.eesti_flag),
                            alignment = Alignment.Center,
                            modifier = modifier.wrapContentSize(),
                            contentDescription = stringResource(id = R.string.main_about_1_logo_text),
                        )
                        Text(
                            modifier = modifier.notAccessible(),
                            text = stringResource(id = R.string.main_about_1_logo_text),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                        )
                        Image(
                            painter = painterResource(id = R.drawable.eu_flag),
                            alignment = Alignment.Center,
                            modifier = modifier.wrapContentSize(),
                            contentDescription = stringResource(id = R.string.main_about_2_logo_text),
                        )
                        Text(
                            modifier = modifier.notAccessible(),
                            text = stringResource(id = R.string.main_about_2_logo_text),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Column(
                        modifier =
                            modifier
                                .padding(start = XSPadding)
                                .weight(.65f),
                        horizontalAlignment = Alignment.Start,
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
                        Text(
                            modifier = modifier.padding(vertical = SPadding),
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
                                modifier
                                    .shadow(
                                        elevation = MCornerRadius,
                                        shape = buttonRoundCornerShape,
                                        ambientColor = MaterialTheme.colorScheme.primary,
                                        spotColor = MaterialTheme.colorScheme.primary,
                                    )
                                    .clip(buttonRoundCornerShape)
                                    .semantics {
                                        contentDescription = helpButtonContentDescriptionText
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("mainInfoHelpButton"),
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
                                        Uri.parse(context.getString(R.string.main_home_menu_help_url)),
                                    )

                                context.startActivity(browserIntent, null)
                            },
                        ) {
                            Icon(
                                modifier = modifier.size(iconSizeXXS),
                                imageVector =
                                    ImageVector.vectorResource(
                                        id = R.drawable.ic_m3_open_in_new_48dp_wght400,
                                    ),
                                contentDescription = null,
                            )
                            Spacer(modifier = modifier.width(XSPadding))
                            Text(
                                modifier = modifier.notAccessible(),
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
                            )
                            .semantics { heading() },
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
        )
    }
}
