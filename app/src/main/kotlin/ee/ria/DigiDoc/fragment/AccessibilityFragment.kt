@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.AccessibilityScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccessibilityFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("accessibilityFragment"),
        color = MaterialTheme.colorScheme.background,
    ) {
        AccessibilityScreen(
            navController = navController,
            modifier = modifier,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccessibilityFragmentPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        AccessibilityFragment(navController)
    }
}
