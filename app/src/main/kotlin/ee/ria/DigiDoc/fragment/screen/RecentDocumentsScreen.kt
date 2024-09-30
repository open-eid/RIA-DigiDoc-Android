@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.dialog.SivaConfirmationDialog
import ee.ria.DigiDoc.ui.component.signing.Document
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utilsLib.extensions.isCades
import ee.ria.DigiDoc.utilsLib.extensions.isXades
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil
import ee.ria.DigiDoc.viewmodel.RecentDocumentsViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentDocumentsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedContainerViewModel: SharedContainerViewModel,
    recentDocumentsViewModel: RecentDocumentsViewModel = hiltViewModel(),
) {
    val logTag = "RecentDocumentsScreen"

    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)

    val showLoading = remember { mutableStateOf(false) }
    val showSivaDialog = remember { mutableStateOf(false) }
    val selectedDocument = remember { mutableStateOf<File?>(null) }

    val handleResult: (Boolean) -> Unit = { confirmed ->
        showLoading.value = true
        val document = selectedDocument.value

        if (document == null) {
            showLoading.value = false
        } else {
            CoroutineScope(IO).launch {
                try {
                    val documentMimeType = document.mimeType(context)

                    recentDocumentsViewModel.handleDocument(
                        document,
                        documentMimeType,
                        confirmed,
                        sharedContainerViewModel,
                    )
                } catch (ex: Exception) {
                    recentDocumentsViewModel.handleError(logTag, ex)
                } finally {
                    showLoading.value = false
                }
            }
        }
    }

    val recentDocumentList =
        remember {
            mutableStateOf(
                recentDocumentsViewModel.getRecentDocumentList(),
            )
        }
    var actionDocument by remember { mutableStateOf<File?>(null) }
    val openRemoveDocumentDialog = remember { mutableStateOf(false) }

    val documentRemoved = stringResource(id = R.string.document_removed)
    val documentRemovalCancelled = stringResource(id = R.string.document_removal_cancelled)
    val removeSignatureDialogTitle =
        stringResource(id = R.string.recent_documents_remove_confirmation_message)
    val closeDocumentDialog = {
        openRemoveDocumentDialog.value = false
    }
    val dismissRemoveDocumentDialog = {
        closeDocumentDialog()
        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, documentRemovalCancelled)
    }

    LaunchedEffect(recentDocumentsViewModel.sendToSigningViewWithSiva) {
        recentDocumentsViewModel.sendToSigningViewWithSiva.asFlow().collect { openSigningView ->
            if (openSigningView) {
                recentDocumentsViewModel.handleSendToSigningViewWithSiva(false)
                navController.navigate(Route.Signing.route)
            }
        }
    }

    LaunchedEffect(recentDocumentsViewModel.errorState) {
        recentDocumentsViewModel.errorState.asFlow().collect { error ->
            error?.let {
                ToastUtil.showMessage(
                    context,
                    error,
                )
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.recent_documents_title,
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
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (recentDocumentList.value.isNotEmpty()) {
                HorizontalDivider(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(screenViewLargePadding)
                            .height(dividerHeight),
                )
                recentDocumentList.value.forEach { document ->
                    Document(
                        name = document.name,
                        onItemClick = {
                            CoroutineScope(IO).launch {
                                selectedDocument.value = document
                                if ((
                                        SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(
                                            document.mimeType(context),
                                        ) || document.isCades(context)
                                    ) && !document.isXades(context)
                                ) {
                                    showSivaDialog.value = true
                                } else {
                                    showSivaDialog.value = false
                                    val signedContainer = recentDocumentsViewModel.openDocument(document, true)
                                    sharedContainerViewModel.setSignedContainer(signedContainer)

                                    withContext(Main) {
                                        navController.navigate(
                                            Route.Signing.route,
                                        )
                                    }
                                }
                            }
                        },
                        onRemoveButtonClick = {
                            actionDocument = document
                            openRemoveDocumentDialog.value = true
                        },
                    )
                    HorizontalDivider(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(screenViewLargePadding)
                                .height(dividerHeight),
                    )
                }
            } else {
                Box(
                    modifier =
                        modifier
                            .padding(screenViewLargePadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.recent_documents_empty_message),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            if (openRemoveDocumentDialog.value) {
                BasicAlertDialog(
                    onDismissRequest = dismissRemoveDocumentDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = screenViewLargePadding),
                    ) {
                        MessageDialog(
                            title = removeSignatureDialogTitle,
                            cancelButtonClick = dismissRemoveDocumentDialog,
                            okButtonClick = {
                                actionDocument?.delete()
                                recentDocumentList.value = recentDocumentsViewModel.getRecentDocumentList()
                                closeDocumentDialog()
                                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, documentRemoved)
                            },
                        )
                    }
                }
            }
        }

        SivaConfirmationDialog(
            showDialog = showSivaDialog,
            modifier = modifier,
            onResult = handleResult,
        )

        if (showLoading.value) {
            LoadingScreen()
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecentDocumentsScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        RecentDocumentsScreen(
            sharedContainerViewModel = hiltViewModel(),
            navController = navController,
        )
    }
}
