@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccessibilityScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

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
                }.testTag("accessibilityScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_accessibility_title,
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
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag("scrollView")
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroduction"),
                    text = stringResource(R.string.main_accessibility_introduction),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityLink"),
                    text = stringResource(R.string.main_accessibility_link),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroduction2"),
                    text = stringResource(R.string.main_accessibility_introduction_2),
                )
                Text(
                    modifier =
                        modifier
                            .padding(
                                start = SPadding,
                                top = MPadding,
                                end = SPadding,
                            ).semantics { heading() }
                            .testTag("mainAccessibilityIntroductionScreenReaderTitle"),
                    text = stringResource(id = R.string.main_accessibility_introduction_screen_reader_title),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge,
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroduction"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroduction2"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_2),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroductionApps"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_apps),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroductionIos"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_ios),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroductionAndroid"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_android),
                )
                Text(
                    modifier =
                        modifier
                            .padding(
                                start = SPadding,
                                top = MPadding,
                                end = SPadding,
                            ).semantics { heading() }
                            .testTag("mainAccessibilityIntroductionScreenMagnificationTitle"),
                    text = stringResource(id = R.string.main_accessibility_introduction_screen_magnification_title),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge,
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationIntroduction"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_introduction),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationScreenTools"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_screen_tools),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationScreenToolsIos"),
                    text =
                        stringResource(
                            R.string.main_accessibility_introduction_screen_magnification_screen_tools_ios,
                        ),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationScreenToolsAndroid"),
                    text =
                        stringResource(
                            R.string.main_accessibility_introduction_screen_magnification_screen_tools_android,
                        ),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationTools"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationToolsIos"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_ios),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationToolsAndroid"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_android),
                )
                InvisibleElement(modifier = modifier)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccessibilityScreenPreview() {
    RIADigiDocTheme {
        AccessibilityScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
        )
    }
}
