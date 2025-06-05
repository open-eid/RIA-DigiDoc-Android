@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.notifications.ContainerNotificationType
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContainerNotificationsScreen(
    modifier: Modifier,
    navController: NavHostController,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedMenuViewModel: SharedMenuViewModel,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val containerNotifications by sharedContainerViewModel.containerNotifications.collectAsState()

    BackHandler {
        navController.navigateUp()
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
                .testTag("containerNotificationsScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.container_notificaitons,
                onLeftButtonClick = {
                    navController.navigateUp()
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
                showExtraButton = false,
            )
        },
    ) { innerPadding ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.primary)
                    .focusGroup()
                    .semantics {
                        testTagsAsResourceId = true
                    },
        ) {
            Column(
                modifier =
                    modifier
                        .verticalScroll(rememberScrollState())
                        .padding(SPadding)
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("signersCertificateContainer"),
            ) {
                HorizontalDivider()
                containerNotifications.forEach { type ->
                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(vertical = SPadding)
                                .focusable(true)
                                .semantics(mergeDescendants = true) {
                                    testTagsAsResourceId = true
                                }
                                .focusGroup()
                                .testTag("containerNotificationsScreenDataRow"),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_m3_notifications_48dp_wght400),
                            contentDescription = null,
                            modifier =
                                modifier
                                    .size(iconSizeXXS)
                                    .focusable(false)
                                    .semantics {
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("containerNotificationsScreenNotificationIcon")
                                    .notAccessible(),
                        )
                        Spacer(modifier = modifier.width(SPadding))

                        Text(
                            text =
                                when (type) {
                                    ContainerNotificationType.XadesFile -> stringResource(R.string.xades_file_message)
                                    ContainerNotificationType.CadesFile -> stringResource(R.string.cades_file_message)
                                    is ContainerNotificationType.UnknownSignatures ->
                                        pluralStringResource(
                                            id = R.plurals.signatures_unknown,
                                            count = type.count,
                                            type.count,
                                        )
                                    is ContainerNotificationType.InvalidSignatures ->
                                        pluralStringResource(
                                            id = R.plurals.signatures_invalid,
                                            count = type.count,
                                            type.count,
                                        )
                                },
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
