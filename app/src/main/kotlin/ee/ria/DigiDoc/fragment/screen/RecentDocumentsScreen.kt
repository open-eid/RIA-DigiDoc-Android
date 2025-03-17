@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.Constant.SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.PreventResize
import ee.ria.DigiDoc.ui.component.shared.dialog.SivaConfirmationDialog
import ee.ria.DigiDoc.ui.component.signing.Document
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.extensions.isCades
import ee.ria.DigiDoc.utilsLib.extensions.isXades
import ee.ria.DigiDoc.viewmodel.RecentDocumentsViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
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
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val handleResult: (Boolean) -> Unit = { confirmed ->
        showLoading.value = true
        val document = selectedDocument.value

        if (document == null) {
            showLoading.value = false
        } else {
            CoroutineScope(IO).launch {
                try {
                    val documentMimeType = recentDocumentsViewModel.getMimetype(document)

                    recentDocumentsViewModel.handleDocument(
                        document,
                        documentMimeType ?: DEFAULT_CONTAINER_EXTENSION,
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

    val removeSignatureDialogMessage =
        stringResource(id = R.string.recent_documents_remove_confirmation_message)
    val removeSignatureCancelButtonContentDescription =
        stringResource(id = R.string.signature_update_cancel_signature_removal_button)
    val removeSignatureOkButtonContentDescription =
        stringResource(id = R.string.signature_update_confirm_signature_removal_button)
    val closeDocumentDialog = {
        openRemoveDocumentDialog.value = false
    }
    val dismissRemoveDocumentDialog = {
        closeDocumentDialog()
        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, documentRemovalCancelled)
    }

    val listState = rememberLazyListState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val searchText by recentDocumentsViewModel.searchText.collectAsState()
    val documentList by recentDocumentsViewModel.documentList.collectAsState()

    val dismissSearch = {
        expanded = false
        recentDocumentsViewModel.onSearchTextChange("")
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
                showMessage(context, error)
            }
        }
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("recentDocumentsScreen"),
        topBar = {
            if (!expanded) {
                TopBar(
                    modifier = modifier,
                    title = null,
                    onLeftButtonClick = {
                        navController.navigateUp()
                    },
                    onRightSecondaryButtonClick = {
                        isSettingsMenuBottomSheetVisible.value = true
                    },
                )
            }
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )

        Column(
            modifier =
                modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!expanded) {
                Text(
                    text = stringResource(id = R.string.recent_documents_title),
                    maxLines = 2,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(SPadding)
                            .semantics { heading() }
                            .focusable(enabled = true)
                            .focusTarget()
                            .focusProperties { canFocus = true },
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            val searchBarPadding =
                if (!expanded) {
                    SPadding
                } else {
                    zeroPadding
                }
            SearchBar(
                modifier =
                    modifier
                        .padding(horizontal = searchBarPadding),
                inputField = {
                    SearchBarDefaults.InputField(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                        query = searchText,
                        onQueryChange = recentDocumentsViewModel::onSearchTextChange,
                        onSearch = recentDocumentsViewModel::onSearchTextChange,
                        expanded = expanded,
                        enabled = true,
                        placeholder = {
                            PreventResize {
                                Text(stringResource(id = R.string.recent_documents_search))
                            }
                        },
                        leadingIcon = {
                            Icon(
                                modifier =
                                    modifier
                                        .size(iconSizeXXS),
                                imageVector = ImageVector.vectorResource(R.drawable.ic_m3_search_48dp_wght400),
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                modifier =
                                    modifier
                                        .padding(end = XSPadding)
                                        .size(iconSizeXXS)
                                        .testTag("searchCancelButton"),
                                onClick = dismissSearch,
                                content = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_m3_close_48dp_wght400),
                                        contentDescription =
                                            stringResource(
                                                id = R.string.recent_documents_search_cancel,
                                            ),
                                    )
                                },
                            )
                        },
                        onExpandedChange = { expanded = it },
                        colors = inputFieldColors(),
                        interactionSource = null,
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                LazyColumn(
                    state = listState,
                    modifier = modifier.testTag("lazyColumnScrollView"),
                ) {
                    if (documentList.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(SPadding)
                                        .height(dividerHeight),
                            )
                        }
                        items(documentList) { document ->
                            Document(
                                name = document.name,
                                onItemClick = {
                                    CoroutineScope(IO).launch {
                                        selectedDocument.value = document
                                        if ((
                                                SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(
                                                    recentDocumentsViewModel.getMimetype(
                                                        document,
                                                    ),
                                                ) || document.isCades(context)
                                            ) && !document.isXades(context)
                                        ) {
                                            showSivaDialog.value = true
                                        } else {
                                            showSivaDialog.value = false
                                            val signedContainer =
                                                recentDocumentsViewModel.openDocument(
                                                    document,
                                                    true,
                                                )
                                            sharedContainerViewModel.setSignedContainer(
                                                signedContainer,
                                            )

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
                                        .padding(SPadding)
                                        .height(dividerHeight),
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier =
                                    modifier
                                        .padding(SPadding),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    modifier =
                                        modifier
                                            .testTag("recentDocumentsListEmpty"),
                                    text = stringResource(id = R.string.recent_documents_search_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }

                    item {
                        Spacer(
                            modifier = modifier.height(invisibleElementHeight),
                        )
                        if (listState.reachedBottom()) {
                            InvisibleElement(modifier = modifier)
                        }
                    }
                }
            }
            if (!expanded) {
                LazyColumn(
                    state = listState,
                    modifier = modifier.testTag("lazyColumnScrollView"),
                ) {
                    if (recentDocumentList.value.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(SPadding)
                                        .height(dividerHeight),
                            )
                        }
                        items(recentDocumentList.value) { document ->
                            Document(
                                name = document.name,
                                onItemClick = {
                                    CoroutineScope(IO).launch {
                                        selectedDocument.value = document
                                        if ((
                                                SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(
                                                    recentDocumentsViewModel.getMimetype(
                                                        document,
                                                    ),
                                                ) || document.isCades(context)
                                            ) && !document.isXades(context)
                                        ) {
                                            showSivaDialog.value = true
                                        } else {
                                            showSivaDialog.value = false
                                            val signedContainer =
                                                recentDocumentsViewModel.openDocument(
                                                    document,
                                                    true,
                                                )
                                            sharedContainerViewModel.setSignedContainer(
                                                signedContainer,
                                            )

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
                                        .padding(SPadding)
                                        .height(dividerHeight),
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier =
                                    modifier
                                        .padding(SPadding),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    modifier =
                                        modifier
                                            .testTag("recentDocumentsListEmpty"),
                                    text = stringResource(id = R.string.recent_documents_empty_message),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }

                    item {
                        Spacer(
                            modifier = modifier.height(invisibleElementHeight),
                        )
                        if (listState.reachedBottom()) {
                            InvisibleElement(modifier = modifier)
                        }
                    }
                }
            }
        }
        if (openRemoveDocumentDialog.value) {
            MessageDialog(
                modifier = modifier,
                title = stringResource(R.string.signature_remove_button),
                message = removeSignatureDialogMessage,
                showIcons = false,
                dismissButtonText = stringResource(R.string.cancel_button),
                confirmButtonText = stringResource(R.string.remove_title),
                dismissButtonContentDescription = removeSignatureCancelButtonContentDescription,
                confirmButtonContentDescription = removeSignatureOkButtonContentDescription,
                onDismissRequest = dismissRemoveDocumentDialog,
                onDismissButton = dismissRemoveDocumentDialog,
                onConfirmButton = {
                    actionDocument?.delete()
                    recentDocumentList.value = recentDocumentsViewModel.getRecentDocumentList()
                    dismissSearch()
                    closeDocumentDialog()
                    AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, documentRemoved)
                },
            )
        }

        SivaConfirmationDialog(
            showDialog = showSivaDialog,
            modifier = modifier,
            onResult = handleResult,
        )

        if (showLoading.value) {
            LoadingScreen(modifier = modifier)
        }
    }
}

private fun LazyListState.reachedBottom(): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem != null && lastVisibleItem.index != 0 &&
        this.layoutInfo.totalItemsCount > 5 &&
        lastVisibleItem.index == this.layoutInfo.totalItemsCount - 1
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
