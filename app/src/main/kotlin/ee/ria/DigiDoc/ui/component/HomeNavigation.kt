@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.Tasks
import ee.ria.DigiDoc.fragment.screen.CryptoScreen
import ee.ria.DigiDoc.fragment.screen.MyEIDScreen
import ee.ria.DigiDoc.fragment.screen.SignatureScreen
import ee.ria.DigiDoc.ui.component.main.CrashDialog
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@Composable
fun HomeNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onClickMenu: () -> Unit = {},
    onClickToFileChoosingScreen: () -> Unit = {},
    onClickToRecentDocumentsScreen: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val openCrashDetectorDialog = remember { mutableStateOf(false) }
    val hasUnsentReports by homeViewModel.hasUnsentReports.asFlow().collectAsState(Tasks.forResult(false))

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(homeViewModel.didAppCrashOnPreviousExecution(), hasUnsentReports) {
        if (!homeViewModel.isCrashSendingAlwaysEnabled()) {
            openCrashDetectorDialog.value = true
        } else {
            CoroutineScope(IO).launch {
                homeViewModel.sendUnsentReports()
            }
        }
    }

    if (openCrashDetectorDialog.value && !homeViewModel.isCrashSendingAlwaysEnabled() &&
        (homeViewModel.didAppCrashOnPreviousExecution() || hasUnsentReports.result)
    ) {
        CrashDialog(
            onDontSendClick = {
                openCrashDetectorDialog.value = false
                homeViewModel.deleteUnsentReports()
            },
            onSendClick = {
                openCrashDetectorDialog.value = false
                CoroutineScope(IO).launch {
                    homeViewModel.sendUnsentReports()
                }
            },
            onAlwaysSendClick = {
                openCrashDetectorDialog.value = false
                homeViewModel.setCrashSendingAlwaysEnabled(true)
                CoroutineScope(IO).launch {
                    homeViewModel.sendUnsentReports()
                }
            },
        )
    } else if (homeViewModel.isCrashSendingAlwaysEnabled()) {
        openCrashDetectorDialog.value = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize().focusGroup(),
        topBar = {
            HomeToolbar(
                modifier =
                    modifier
                        .semantics {
                            isTraversalGroup = true
                            traversalIndex = 2f
                        },
                onClickMenu = onClickMenu,
            )
        },
        bottomBar = {
            HomeNavigationBar(
                modifier =
                    modifier
                        .semantics {
                            isTraversalGroup = true
                            traversalIndex = 1f
                        },
                navController = navController,
            )
        },
    ) { paddingValues ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Signature.route,
                modifier =
                    modifier
                        .padding(paddingValues)
                        .focusable(),
            ) {
                composable(Route.Signature.route) {
                    SignatureScreen(
                        onClickToFileChoosingScreen = onClickToFileChoosingScreen,
                        onClickToRecentDocumentsScreen = onClickToRecentDocumentsScreen,
                        focusRequester = focusRequester,
                        modifier =
                            modifier
                                .semantics {
                                    isTraversalGroup = true
                                    traversalIndex = 0f
                                }
                                .focusRequester(focusRequester),
                    )
                }
                composable(Route.Crypto.route) {
                    CryptoScreen(navController = navController)
                }
                composable(Route.EID.route) {
                    MyEIDScreen(navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeNavigationPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        HomeNavigation(
            navController = navController,
        )
    }
}
