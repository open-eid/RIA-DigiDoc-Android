@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccessibilityScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("accessibilityScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_accessibility_title,
                onBackButtonClick = {
                    navController.navigateUp()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .testTag("scrollView"),
            horizontalAlignment = Alignment.Start,
        ) {
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroduction"),
                text = stringResource(R.string.main_accessibility_introduction),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityLink"),
                text = stringResource(R.string.main_accessibility_link),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroduction2"),
                text = stringResource(R.string.main_accessibility_introduction_2),
            )
            Text(
                modifier =
                    modifier
                        .padding(
                            start = screenViewSmallPadding,
                            top = screenViewExtraLargePadding,
                            end = screenViewSmallPadding,
                        )
                        .semantics { heading() }
                        .testTag("mainAccessibilityIntroductionScreenReaderTitle"),
                text = stringResource(id = R.string.main_accessibility_introduction_screen_reader_title),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleLarge,
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenReaderIntroduction"),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenReaderIntroduction2"),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_2),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenReaderIntroductionApps"),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_apps),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenReaderIntroductionIos"),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_ios),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenReaderIntroductionAndroid"),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_android),
            )
            Text(
                modifier =
                    modifier
                        .padding(
                            start = screenViewSmallPadding,
                            top = screenViewExtraLargePadding,
                            end = screenViewSmallPadding,
                        )
                        .semantics { heading() }
                        .testTag("mainAccessibilityIntroductionScreenMagnificationTitle"),
                text = stringResource(id = R.string.main_accessibility_introduction_screen_magnification_title),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleLarge,
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenMagnificationIntroduction"),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_introduction),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenMagnificationScreenTools"),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_screen_tools),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenMagnificationScreenToolsIos"),
                text =
                    stringResource(
                        R.string.main_accessibility_introduction_screen_magnification_screen_tools_ios,
                    ),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenMagnificationScreenToolsAndroid"),
                text =
                    stringResource(
                        R.string.main_accessibility_introduction_screen_magnification_screen_tools_android,
                    ),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenMagnificationTools"),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenMagnificationToolsIos"),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_ios),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewSmallPadding,
                            vertical = screenViewSmallPadding,
                        )
                        .testTag("mainAccessibilityIntroductionScreenMagnificationToolsAndroid"),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_android),
            )
            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccessibilityScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        AccessibilityScreen(
            navController = navController,
        )
    }
}
