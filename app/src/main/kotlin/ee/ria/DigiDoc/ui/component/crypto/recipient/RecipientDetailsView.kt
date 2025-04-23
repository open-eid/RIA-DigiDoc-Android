@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.recipient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.DigiDoc.viewmodel.CertificateDetailViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RecipientDetailsView(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedRecipientViewModel: SharedRecipientViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    certificateDetailViewModel: CertificateDetailViewModel = hiltViewModel(),
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val recipient = sharedRecipientViewModel.recipient.value
    val recipientIssuerName =
        certificateDetailViewModel.getIssuerCommonName(
            recipient?.data?.x509Certificate(),
        )

    BackHandler {
        handleBackButtonClick(navController, sharedRecipientViewModel)
    }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    if (recipient != null) {
        val recipientFormattedName = formatName(recipient.surname, recipient.givenName, recipient.identifier)
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
                    .testTag("recipientDetailsScreen"),
            topBar = {
                TopBar(
                    modifier = modifier,
                    sharedMenuViewModel = sharedMenuViewModel,
                    title = R.string.recipient_details_title,
                    onLeftButtonClick = {
                        handleBackButtonClick(navController, sharedRecipientViewModel)
                    },
                    onRightSecondaryButtonClick = {
                        isSettingsMenuBottomSheetVisible.value = true
                    },
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
                            .testTag("recipientCertificateContainer"),
                ) {
                    RecipientDetails(
                        modifier = modifier,
                        recipient = recipient,
                        recipientFormattedName = recipientFormattedName,
                        recipientIssuerName = recipientIssuerName,
                        sharedCertificateViewModel = sharedCertificateViewModel,
                        navController = navController,
                    )
                    InvisibleElement(modifier = modifier)
                }
            }
        }
    }
}

private fun handleBackButtonClick(
    navController: NavController,
    sharedRecipientViewModel: SharedRecipientViewModel,
) {
    sharedRecipientViewModel.resetRecipient()
    navController.navigateUp()
}
