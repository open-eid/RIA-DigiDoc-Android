@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.component.shared.LinkifyText
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    Text(
                        text = stringResource(id = R.string.main_accessibility_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    BackButton(
                        onClickBack = {
                            navController.navigateUp()
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_link),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_2),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            Text(
                modifier =
                    modifier.padding(
                        start = screenViewSmallPadding,
                        top = screenViewExtraLargePadding,
                        end = screenViewSmallPadding,
                    ),
                text = stringResource(id = R.string.main_accessibility_introduction_screen_reader_title),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleLarge,
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_2),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_apps),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_ios),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_android),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            Text(
                modifier =
                    modifier.padding(
                        start = screenViewSmallPadding,
                        top = screenViewExtraLargePadding,
                        end = screenViewSmallPadding,
                    ),
                text = stringResource(id = R.string.main_accessibility_introduction_screen_magnification_title),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleLarge,
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_introduction),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_screen_tools),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text =
                    stringResource(
                        R.string.main_accessibility_introduction_screen_magnification_screen_tools_ios,
                    ),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text =
                    stringResource(
                        R.string.main_accessibility_introduction_screen_magnification_screen_tools_android,
                    ),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_ios),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
            LinkifyText(
                modifier =
                    modifier.padding(
                        horizontal = screenViewSmallPadding,
                        vertical = screenViewSmallPadding,
                    ),
                text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_android),
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_START,
                textColor = MaterialTheme.colorScheme.primary.toArgb(),
                linkTextColor = Blue500.toArgb(),
            )
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
