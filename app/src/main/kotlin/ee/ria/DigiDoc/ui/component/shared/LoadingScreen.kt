@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

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
    ) { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusGroup()
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("loadingScreen"),
        ) {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(vertical = screenViewExtraLargePadding)
                        .testTag("activityOverlay"),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier =
                        modifier
                            .size(loadingBarSize)
                            .testTag("activityIndicator"),
                )
            }
        }
    }
}
