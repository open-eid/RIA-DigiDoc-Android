@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.LocalActivity
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
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WebEidFragment(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    webEidUri: Uri?,
    viewModel: WebEidViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel = hiltViewModel(),
    sharedMenuViewModel: SharedMenuViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current as Activity

    LaunchedEffect(viewModel) {
        viewModel.relyingPartyResponseEvents.collect { responseUri ->
            val browserIntent =
                Intent(Intent.ACTION_VIEW, responseUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            activity.startActivity(browserIntent)
            activity.finishAndRemoveTask()
        }
    }

    LaunchedEffect(webEidUri) {
        webEidUri?.let {
            when (it.host) {
                "auth" -> viewModel.handleAuth(it)
                "sign" -> viewModel.handleSign(it)
                else -> {
                    viewModel.handleUnknown(it)
                }
            }
        }
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
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedMenuViewModel = sharedMenuViewModel,
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
            webEidUri = null,
        )
    }
}
