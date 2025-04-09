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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.ui.component.crypto.bottombar.EncryptBottomBar
import ee.ria.DigiDoc.ui.component.crypto.bottomsheet.RecipientBottomSheet
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.ContainerMessage
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.PreventResize
import ee.ria.DigiDoc.ui.component.shared.Recipient
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.Green500
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.extensions.reachedBottom
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.EncryptRecipientViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EncryptRecipientScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedRecipientViewModel: SharedRecipientViewModel,
    encryptRecipientViewModel: EncryptRecipientViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())
    val cryptoContainer by sharedContainerViewModel.cryptoContainer.asFlow().collectAsState(null)

    val showLoading = remember { mutableStateOf(false) }
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val recipientAddedSuccess = remember { mutableStateOf(false) }
    val recipientAddedSuccessText = stringResource(id = R.string.crypto_recipients_recipient_add_success)

    val containerRecipientList =
        remember {
            mutableStateOf(
                encryptRecipientViewModel.getContainerRecipientList(sharedContainerViewModel),
            )
        }
    val showRecipientBottomSheet = remember { mutableStateOf(false) }
    var actionRecipient by remember { mutableStateOf<Addressee?>(null) }
    val clickedRecipient = remember { mutableStateOf<Addressee?>(null) }
    val openRemoveRecipientDialog = remember { mutableStateOf(false) }

    val recipientRemoved = stringResource(id = R.string.recipient_removed)
    val recipientRemovalCancelled = stringResource(id = R.string.recipient_removal_cancelled)

    val removeRecipientDialogMessage =
        stringResource(id = R.string.crypto_recipient_remove_confirmation_message)
    val removeRecipientCancelButtonContentDescription =
        stringResource(id = R.string.crypto_cancel_recipient_removal_button)
    val removeRecipientOkButtonContentDescription =
        stringResource(id = R.string.crypto_confirm_recipient_removal_button)
    val closeRecipientDialog = {
        openRemoveRecipientDialog.value = false
    }
    val dismissRemoveRecipientDialog = {
        closeRecipientDialog()
        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, recipientRemovalCancelled)
    }

    val listState = rememberLazyListState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val searchText by encryptRecipientViewModel.searchText.collectAsState()
    val recipientList by encryptRecipientViewModel.recipientList.collectAsState()

    val dismissSearch = {
        expanded = false
        encryptRecipientViewModel.onSearchTextChange("")
    }

    LaunchedEffect(encryptRecipientViewModel.isRecipientAdded) {
        encryptRecipientViewModel.isRecipientAdded.asFlow().collect { isRecipientAdded ->
            if (isRecipientAdded) {
                withContext(Main) {
                    recipientAddedSuccess.value = true
                    containerRecipientList.value =
                        encryptRecipientViewModel
                            .getContainerRecipientList(sharedContainerViewModel)
                    AccessibilityUtil.sendAccessibilityEvent(
                        context,
                        TYPE_ANNOUNCEMENT,
                        recipientAddedSuccessText,
                    )
                    delay(3000)
                    recipientAddedSuccess.value = false
                    encryptRecipientViewModel.handleIsRecipientAdded(false)
                }
            }
        }
    }

    LaunchedEffect(encryptRecipientViewModel.errorState) {
        encryptRecipientViewModel.errorState.asFlow().collect { error ->
            error?.let {
                showMessage(context, error)
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
                .testTag("encryptRecipientsScreen"),
        topBar = {
            if (!expanded) {
                TopBar(
                    modifier = modifier,
                    sharedMenuViewModel = sharedMenuViewModel,
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
        bottomBar = {
            EncryptBottomBar(
                modifier = modifier,
                onEncryptClick = {
                    // TODO: Implement encrypt action
                },
            )
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
            horizontalAlignment = Alignment.Start,
        ) {
            if (recipientAddedSuccess.value) {
                ContainerMessage(
                    modifier = modifier,
                    text = recipientAddedSuccessText,
                    testTag = "recipientAddedSuccess",
                    color = Green500,
                )
            }
            if (!expanded) {
                Text(
                    text = stringResource(id = R.string.crypto_container_recipients_title),
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
                        onQueryChange = encryptRecipientViewModel::onSearchTextChange,
                        onSearch = encryptRecipientViewModel::onQueryTextChange,
                        expanded = expanded,
                        enabled = true,
                        placeholder = {
                            PreventResize {
                                Text(stringResource(id = R.string.crypto_recipients_search))
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
                                                id = R.string.crypto_recipients_search_cancel,
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
                    if (recipientList.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(SPadding)
                                        .height(dividerHeight),
                            )
                        }
                        items(recipientList) { recipient ->
                            Recipient(
                                recipient = recipient,
                                onClick = {
                                    encryptRecipientViewModel.addRecipientToContainer(
                                        recipient,
                                        sharedContainerViewModel,
                                    )
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
                                            .testTag("encryptRecipientsListEmpty"),
                                    text = stringResource(id = R.string.crypto_recipients_search_empty),
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
                    item {
                        Text(
                            modifier =
                                modifier
                                    .padding(horizontal = SPadding)
                                    .padding(top = SPadding)
                                    .semantics {
                                        heading()
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("encryptRecipientsDescription"),
                            text = stringResource(R.string.crypto_recipients_description),
                            textAlign = TextAlign.Start,
                        )
                    }
                    if (containerRecipientList.value.isNotEmpty()) {
                        item {
                            Text(
                                modifier =
                                    modifier
                                        .padding(horizontal = SPadding)
                                        .padding(top = SPadding)
                                        .semantics {
                                            heading()
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("encryptRecipientsListTitle"),
                                text = stringResource(R.string.crypto_container_added_recipients_title),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Start,
                            )
                        }
                        item {
                            HorizontalDivider(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(SPadding)
                                        .height(dividerHeight),
                            )
                        }
                        items(containerRecipientList.value) { recipient ->
                            Recipient(
                                recipient = recipient,
                                onClick = {
                                    clickedRecipient.value = recipient
                                    showRecipientBottomSheet.value = true
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
        }

        if (openRemoveRecipientDialog.value) {
            MessageDialog(
                modifier = modifier,
                title = stringResource(R.string.recipient_remove_button),
                message = removeRecipientDialogMessage,
                showIcons = false,
                dismissButtonText = stringResource(R.string.cancel_button),
                confirmButtonText = stringResource(R.string.remove_title),
                dismissButtonContentDescription = removeRecipientCancelButtonContentDescription,
                confirmButtonContentDescription = removeRecipientOkButtonContentDescription,
                onDismissRequest = dismissRemoveRecipientDialog,
                onDismissButton = dismissRemoveRecipientDialog,
                onConfirmButton = {
                    CoroutineScope(IO).launch {
                        sharedContainerViewModel.removeRecipient(cryptoContainer, actionRecipient)
                    }
                    containerRecipientList.value =
                        encryptRecipientViewModel
                            .getContainerRecipientList(sharedContainerViewModel)
                    dismissSearch()
                    closeRecipientDialog()
                    AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, recipientRemoved)
                },
            )
        }

        if (showLoading.value) {
            LoadingScreen(modifier = modifier)
        }

        RecipientBottomSheet(
            modifier = modifier,
            showSheet = showRecipientBottomSheet,
            clickedRecipient = clickedRecipient,
            sharedRecipientViewModel = sharedRecipientViewModel,
            navController = navController,
            isRecipientRemoveShown = false,
            openRemoveRecipientDialog = openRemoveRecipientDialog,
            onRecipientRemove = { actionRecipient = it },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptRecipientScreenPreview() {
    RIADigiDocTheme {
        EncryptRecipientScreen(
            sharedMenuViewModel = hiltViewModel(),
            sharedContainerViewModel = hiltViewModel(),
            sharedRecipientViewModel = hiltViewModel(),
            navController = rememberNavController(),
        )
    }
}
