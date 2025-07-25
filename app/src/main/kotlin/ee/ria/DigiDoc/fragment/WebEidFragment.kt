@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.WebEidScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.WebEidViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WebEidFragment(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    webEidUri: Uri?,
    viewModel: WebEidViewModel = hiltViewModel(),
) {
    LaunchedEffect(webEidUri) {
        println("DEBUG: WebEidFragment got URI = $webEidUri")
        webEidUri?.let { viewModel.handleAuth(it) }
    }

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { testTagsAsResourceId = true }
                .testTag("webEidFragment"),
        color = MaterialTheme.colorScheme.background,
    ) {
        WebEidScreen(
            modifier = modifier,
            navController = navController,
            viewModel = viewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WebEidFragmentPreview() {
    RIADigiDocTheme {
        WebEidFragment(
            navController = rememberNavController(),
            webEidUri = null
        )
    }
}