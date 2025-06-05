@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.Tasks
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.home.ActionButton
import ee.ria.DigiDoc.ui.component.main.CrashDialog
import ee.ria.DigiDoc.ui.component.menu.MainMenuBottomSheet
import ee.ria.DigiDoc.ui.component.menu.OpenMenuBottomSheet
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeM
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.HomeViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val openCrashDetectorDialog = remember { mutableStateOf(false) }
    val hasUnsentReports by homeViewModel.hasUnsentReports.asFlow().collectAsState(Tasks.forResult(false))

    val isMainMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val isOpenMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    var openMenuAddFileNavigateTo = Route.FileChoosing.route

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    BackHandler {
        isMainMenuBottomSheetVisible.value = false
        isSettingsMenuBottomSheetVisible.value = false
    }

    LaunchedEffect(homeViewModel.didAppCrashOnPreviousExecution(), hasUnsentReports) {
        if (!homeViewModel.isCrashSendingAlwaysEnabled() && hasUnsentReports.result) {
            openCrashDetectorDialog.value = true
        } else if (homeViewModel.isCrashSendingAlwaysEnabled()) {
            CoroutineScope(IO).launch {
                homeViewModel.sendUnsentReports()
            }
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
        snackbarHost = {
            SnackbarHost(
                modifier = modifier.padding(vertical = SPadding),
                hostState = snackBarHostState,
            )
        },
        modifier =
            modifier
                .fillMaxSize()
                .focusGroup()
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("homeScreen"),
        topBar = {
            TopBar(
                modifier =
                    modifier
                        .semantics {
                            isTraversalGroup = true
                            traversalIndex = 2f
                        },
                sharedMenuViewModel = sharedMenuViewModel,
                leftIcon = R.drawable.ic_m3_menu_48dp_wght400,
                title = null,
                onLeftButtonClick = {
                    isMainMenuBottomSheetVisible.value = true
                    isSettingsMenuBottomSheetVisible.value = false
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                    isMainMenuBottomSheetVisible.value = false
                },
            )
        },
    ) { paddingValues ->
        MainMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isMainMenuBottomSheetVisible,
        )

        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )

        OpenMenuBottomSheet(
            isBottomSheetVisible = isOpenMenuBottomSheetVisible,
            firstButtonClick = {
                isOpenMenuBottomSheetVisible.value = false
                navController.navigate(
                    openMenuAddFileNavigateTo,
                )
            },
            secondButtonClick = {
                isOpenMenuBottomSheetVisible.value = false
                navController.navigate(
                    Route.RecentDocuments.route,
                )
            },
        )

        Surface(
            color = MaterialTheme.colorScheme.surface,
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
                        }
                        .testTag("scrollView")
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = CenterHorizontally,
            ) {
                Box(
                    modifier =
                        modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(horizontal = SPadding, vertical = XSPadding),
                ) {
                    Column(
                        modifier =
                            modifier
                                .wrapContentSize()
                                .align(Center),
                        horizontalAlignment = CenterHorizontally,
                    ) {
                        Row(
                            modifier =
                                modifier
                                    .wrapContentSize(),
                            verticalAlignment = CenterVertically,
                        ) {
                            Image(
                                painterResource(id = R.drawable.image_id_ee),
                                contentDescription = stringResource(id = R.string.digidoc),
                                modifier =
                                    modifier
                                        .padding(end = XSPadding)
                                        .width(iconSizeM),
                            )
                            Text(
                                modifier = modifier,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                text = stringResource(id = R.string.digidoc),
                            )
                        }
                        Text(
                            modifier = modifier.padding(XSPadding),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            text =
                                String.format(
                                    stringResource(id = R.string.main_home_version),
                                    BuildConfig.VERSION_NAME + '.' + BuildConfig.VERSION_CODE,
                                ),
                        )
                    }
                }
                Box(
                    modifier =
                        modifier
                            .wrapContentSize()
                            .padding(horizontal = SPadding, vertical = XSPadding),
                ) {
                    Column(
                        modifier =
                            modifier
                                .wrapContentSize()
                                .align(Center),
                        horizontalAlignment = CenterHorizontally,
                    ) {
                        ActionButton(
                            modifier = modifier,
                            icon = R.drawable.ic_m3_attach_file_48dp_wght400,
                            title = R.string.main_home_open_document_title,
                            description = stringResource(R.string.main_home_open_document_description),
                            contentDescription =
                                stringResource(id = R.string.main_home_open_document_title) + " " +
                                    stringResource(id = R.string.main_home_open_document_description),
                            onClickItem = {
                                openMenuAddFileNavigateTo = Route.FileChoosing.route
                                isOpenMenuBottomSheetVisible.value = true
                            },
                            testTag = "homeOpenDocumentButton",
                        )
                        ActionButton(
                            modifier = modifier,
                            icon = R.drawable.ic_m3_stylus_note_48dp_wght400,
                            title = R.string.main_home_signature_title,
                            description = stringResource(R.string.main_home_signature_description),
                            contentDescription =
                                stringResource(id = R.string.main_home_signature_title) + " " +
                                    stringResource(id = R.string.main_home_signature_description),
                            onClickItem = {
                                openMenuAddFileNavigateTo = Route.FileChoosing.route
                                isOpenMenuBottomSheetVisible.value = true
                            },
                            testTag = "homeSignatureButton",
                        )
                        ActionButton(
                            modifier = modifier,
                            icon = R.drawable.ic_m3_encrypted_48dp_wght400,
                            title = R.string.main_home_crypto_title,
                            description = stringResource(R.string.main_home_crypto_description),
                            contentDescription =
                                stringResource(id = R.string.main_home_crypto_title) + " " +
                                    stringResource(id = R.string.main_home_crypto_description),
                            onClickItem = {
                                openMenuAddFileNavigateTo = Route.CryptoFileChoosing.route
                                isOpenMenuBottomSheetVisible.value = true
                            },
                            testTag = "homeCryptoButton",
                        )
                        ActionButton(
                            modifier = modifier,
                            icon = R.drawable.ic_m3_co_present_48dp_wght400,
                            title = R.string.main_home_my_eid_title,
                            description = stringResource(R.string.main_home_my_eid_description),
                            contentDescription =
                                stringResource(id = R.string.main_home_my_eid_title) + " " +
                                    stringResource(id = R.string.main_home_my_eid_description),
                            onClickItem = {
                                navController.navigate(
                                    Route.MyEidIdentificationScreen.route,
                                )
                            },
                            testTag = "homeMyEIDButton",
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    RIADigiDocTheme {
        HomeScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
        )
    }
}
