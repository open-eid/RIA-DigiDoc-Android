/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.ui.component.crypto.EncryptPasswordDialog
import ee.ria.DigiDoc.ui.component.crypto.bottombar.EncryptBottomBar
import ee.ria.DigiDoc.ui.component.crypto.bottombar.EncryptButtonBottomBar
import ee.ria.DigiDoc.ui.component.crypto.bottomsheet.RecipientBottomSheet
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.PreventResize
import ee.ria.DigiDoc.ui.component.shared.Recipient
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.extensions.reachedBottom
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utils.snackbar.SnackbarType
import ee.ria.DigiDoc.utilsLib.validator.PersonalCodeValidator
import ee.ria.DigiDoc.viewmodel.EncryptRecipientViewModel
import ee.ria.DigiDoc.viewmodel.EncryptViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel
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

    val scope = rememberCoroutineScope()

    val cryptoContainer by sharedContainerViewModel.cryptoContainer.asFlow().collectAsState(null)

    val showLoading = remember { mutableStateOf(false) }
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val showPasswordDialog = rememberSaveable { mutableStateOf(false) }

    val recipientAddedSuccess = remember { mutableStateOf(false) }
    val recipientAddedSuccessText = stringResource(id = R.string.crypto_recipients_recipient_add_success)

    val encryptionButtonEnabled = remember { mutableStateOf(true) }
    val containerEncryptedSuccess = remember { mutableStateOf(false) }
    val containerEncryptedSuccessText = stringResource(id = R.string.crypto_create_success)

    val containerRecipientList =
        remember {
            mutableStateOf(
                encryptRecipientViewModel.getContainerRecipientList(sharedContainerViewModel),
            )
        }
    val showRecipientBottomSheet = remember { mutableStateOf(false) }
    var actionRecipient by rememberSaveable { mutableStateOf<Addressee?>(null) }
    val clickedRecipient = rememberSaveable { mutableStateOf<Addressee?>(null) }
    val openRemoveRecipientDialog = rememberSaveable { mutableStateOf(false) }

    val recipientRemoved = stringResource(id = R.string.recipient_removed)
    val recipientRemovalCancelled = stringResource(id = R.string.recipient_removal_cancelled)

    val removeRecipientDialogMessage =
        stringResource(id = R.string.crypto_recipient_remove_confirmation_message)
    val removeRecipientCancelButtonContentDescription =
        stringResource(id = R.string.crypto_cancel_recipient_removal_button)
    val removeRecipientOkButtonContentDescription =
        stringResource(id = R.string.crypto_confirm_recipient_removal_button)

    val invalidPersonalCodeMessage =
        stringResource(id = R.string.signature_update_mobile_id_invalid_personal_code)

    val closeRecipientDialog = {
        openRemoveRecipientDialog.value = false
    }
    val dismissRemoveRecipientDialog = {
        closeRecipientDialog()
        sendAccessibilityEvent(context, getAccessibilityEventType(), recipientRemovalCancelled)
    }

    val encryptViewModel: EncryptViewModel =
        hiltViewModel(
            viewModelStoreOwner =
                remember {
                    navController.getBackStackEntry(Route.Encrypt.route)
                },
        )
    val isCdoc2 = encryptViewModel.cdocSetting == CDOCSetting.CDOC2
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabRecipientTitle = stringResource(R.string.crypto_encrypt_tab_recipient)
    val tabPasswordTitle = stringResource(R.string.crypto_encrypt_tab_password)

    var expanded by rememberSaveable { mutableStateOf(false) }
    val searchText by encryptRecipientViewModel.searchText.collectAsState()
    val recipientList by encryptRecipientViewModel.recipientList.collectAsState()
    val hasSearched by encryptRecipientViewModel.hasSearched.asFlow().collectAsState(false)

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
                    sendAccessibilityEvent(
                        context,
                        getAccessibilityEventType(),
                        recipientAddedSuccessText,
                    )
                    encryptRecipientViewModel.handleIsRecipientAdded(false)
                }
            }
        }
    }

    LaunchedEffect(encryptRecipientViewModel.isContainerEncrypted) {
        encryptRecipientViewModel.isContainerEncrypted.asFlow().collect { isContainerEncrypted ->
            if (isContainerEncrypted) {
                withContext(Main) {
                    containerEncryptedSuccess.value = true
                    sendAccessibilityEvent(
                        context,
                        getAccessibilityEventType(),
                        containerEncryptedSuccessText,
                    )
                    delay(2000)

                    encryptRecipientViewModel.handleIsContainerEncrypted(false)
                    containerEncryptedSuccess.value = false
                    navController.navigate(Route.Encrypt.route) {
                        popUpTo(Route.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    delay(500)
                    encryptionButtonEnabled.value = true
                }
            }
        }
    }

    LaunchedEffect(encryptRecipientViewModel.errorState) {
        encryptRecipientViewModel.errorState.asFlow().collect { error ->
            error?.let {
                showMessage(context, error)
                encryptionButtonEnabled.value = true
                encryptRecipientViewModel.resetErrorState()
            }
        }
    }

    LaunchedEffect(recipientAddedSuccess.value) {
        if (recipientAddedSuccess.value) {
            showMessage(recipientAddedSuccessText, SnackbarType.SUCCESS)
            recipientAddedSuccess.value = false
        }
    }

    LaunchedEffect(containerEncryptedSuccess.value) {
        if (containerEncryptedSuccess.value) {
            showMessage(containerEncryptedSuccessText, SnackbarType.SUCCESS)
            containerEncryptedSuccess.value = false
        }
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("encryptRecipientsScreen"),
        snackbarHost = { StatusSnackbarHost() },
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
            if (cryptoContainer != null) {
                if (isCdoc2 && selectedTabIndex == 1) {
                    EncryptButtonBottomBar(
                        modifier = modifier,
                        encryptButtonIcon = R.drawable.ic_m3_arrow_forward_48dp_wght400,
                        encryptButtonName = R.string.next_button,
                        encryptButtonContentDescription = R.string.next_button,
                        isEncryptButtonEnabled = true,
                        onEncryptButtonClick = { showPasswordDialog.value = true },
                    )
                } else {
                    EncryptBottomBar(
                        modifier = modifier,
                        isEncryptButtonEnabled = encryptionButtonEnabled.value,
                        onEncryptClick = {
                            if (encryptionButtonEnabled.value) {
                                encryptionButtonEnabled.value = false
                                showLoading.value = true
                                scope.launch(Main) {
                                    encryptRecipientViewModel.encryptContainer(sharedContainerViewModel)
                                    showLoading.value = false
                                }
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )

        if (isCdoc2) {
            Column(
                modifier = modifier.padding(paddingValues).fillMaxSize(),
            ) {
                TabView(
                    modifier = modifier,
                    testTag = "encryptRecipientTabView",
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        selectedTabIndex = index
                        if (index != 0) expanded = false
                    },
                    tabItems =
                        listOf(
                            Pair(tabRecipientTitle) {
                                RecipientTabContent(
                                    modifier = Modifier.fillMaxSize(),
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it },
                                    searchText = searchText,
                                    onSearchTextChange = encryptRecipientViewModel::onSearchTextChange,
                                    invalidPersonalCodeMessage = invalidPersonalCodeMessage,
                                    onSearch = { encryptRecipientViewModel.onQueryTextChange(it) },
                                    onDismissSearch = dismissSearch,
                                    recipientList = recipientList,
                                    hasSearched = hasSearched,
                                    containerRecipientList = containerRecipientList.value,
                                    onAddRecipientToContainer = { recipient ->
                                        encryptRecipientViewModel.addRecipientToContainer(
                                            recipient,
                                            sharedContainerViewModel,
                                        )
                                    },
                                    onRecipientClick = { recipient ->
                                        clickedRecipient.value = recipient
                                        showRecipientBottomSheet.value = true
                                    },
                                )
                            },
                            Pair(tabPasswordTitle) {
                                PasswordTabContent(modifier = Modifier.fillMaxSize())
                            },
                        ),
                )
            }
        } else {
            RecipientTabContent(
                modifier = Modifier.padding(paddingValues).fillMaxWidth(),
                expanded = expanded,
                onExpandedChange = { expanded = it },
                searchText = searchText,
                onSearchTextChange = encryptRecipientViewModel::onSearchTextChange,
                invalidPersonalCodeMessage = invalidPersonalCodeMessage,
                onSearch = { encryptRecipientViewModel.onQueryTextChange(it) },
                onDismissSearch = dismissSearch,
                recipientList = recipientList,
                hasSearched = hasSearched,
                containerRecipientList = containerRecipientList.value,
                onAddRecipientToContainer = { recipient ->
                    encryptRecipientViewModel.addRecipientToContainer(
                        recipient,
                        sharedContainerViewModel,
                    )
                },
                onRecipientClick = { recipient ->
                    clickedRecipient.value = recipient
                    showRecipientBottomSheet.value = true
                },
            )
        }

        if (showPasswordDialog.value) {
            EncryptPasswordDialog(
                modifier = modifier,
                onDismiss = { showPasswordDialog.value = false },
                onEncrypt = { _, _ -> showPasswordDialog.value = false },
            )
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
                    scope.launch(IO) {
                        sharedContainerViewModel.removeRecipient(cryptoContainer, actionRecipient)
                        delay(1000L)
                        containerRecipientList.value =
                            encryptRecipientViewModel
                                .getContainerRecipientList(sharedContainerViewModel)
                    }

                    closeRecipientDialog()
                    sendAccessibilityEvent(context, getAccessibilityEventType(), recipientRemoved)
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
            isRecipientRemoveShown = true,
            openRemoveRecipientDialog = openRemoveRecipientDialog,
            onRecipientRemove = { actionRecipient = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun RecipientTabContent(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    invalidPersonalCodeMessage: String,
    onSearch: (String) -> Unit,
    onDismissSearch: () -> Unit,
    recipientList: List<Addressee>,
    hasSearched: Boolean,
    containerRecipientList: List<Addressee>,
    onAddRecipientToContainer: (Addressee) -> Unit,
    onRecipientClick: (Addressee) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val searchListState = rememberLazyListState()
    val mainListState = rememberLazyListState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        if (!expanded) {
            Text(
                text = stringResource(id = R.string.crypto_container_recipients_title),
                maxLines = 2,
                modifier =
                    Modifier
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
        val searchBarPadding = if (!expanded) SPadding else zeroPadding
        SearchBar(
            modifier = Modifier.padding(horizontal = searchBarPadding),
            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    query = searchText,
                    onQueryChange = onSearchTextChange,
                    onSearch = {
                        if (searchText.isDigitsOnly() &&
                            searchText.length == 11 &&
                            !PersonalCodeValidator.isPersonalCodeValid(searchText)
                        ) {
                            showMessage(invalidPersonalCodeMessage)
                            return@InputField
                        }
                        onSearch(searchText)
                        focusManager.clearFocus()
                    },
                    expanded = expanded,
                    enabled = true,
                    placeholder = {
                        PreventResize {
                            Text(stringResource(id = R.string.crypto_recipients_search))
                        }
                    },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(iconSizeXXS),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_m3_search_48dp_wght400),
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (expanded) {
                            IconButton(
                                modifier =
                                    Modifier
                                        .padding(end = XSPadding)
                                        .size(iconSizeXXS)
                                        .testTag("searchCancelButton"),
                                onClick = onDismissSearch,
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
                        }
                    },
                    onExpandedChange = onExpandedChange,
                    colors = inputFieldColors(),
                    interactionSource = null,
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            LazyColumn(
                state = searchListState,
                modifier = Modifier.testTag("lazyColumnScrollView"),
            ) {
                if (recipientList.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(SPadding)
                                    .height(dividerHeight),
                        )
                    }
                    items(recipientList) { recipient ->
                        RecipientItem(
                            recipient = recipient,
                            isMoreOptionsButtonShown = false,
                            onClick = { onAddRecipientToContainer(it) },
                        )
                    }
                } else if (hasSearched) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillParentMaxSize()
                                    .padding(SPadding),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                modifier = Modifier.testTag("encryptRecipientsListEmpty"),
                                textAlign = TextAlign.Center,
                                text = stringResource(id = R.string.crypto_recipients_search_empty),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
                if (containerRecipientList.isNotEmpty()) {
                    item {
                        Text(
                            modifier =
                                Modifier
                                    .padding(horizontal = SPadding)
                                    .padding(top = SPadding)
                                    .semantics {
                                        heading()
                                        testTagsAsResourceId = true
                                    }.testTag("encryptRecentlyAddedRecipientsListTitle"),
                            text = stringResource(R.string.crypto_container_latest_recipients_title),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                        )
                    }
                    items(containerRecipientList) { recipient ->
                        RecipientItem(
                            recipient = recipient,
                            onClick = { onRecipientClick(it) },
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(invisibleElementHeight))
                    if (searchListState.reachedBottom()) {
                        InvisibleElement(modifier = Modifier)
                    }
                }
            }
        }
        if (!expanded) {
            LazyColumn(
                state = mainListState,
                modifier = Modifier.testTag("lazyColumnScrollView"),
            ) {
                item {
                    Text(
                        modifier =
                            Modifier
                                .padding(horizontal = SPadding)
                                .padding(top = SPadding)
                                .semantics {
                                    heading()
                                    testTagsAsResourceId = true
                                }.testTag("encryptRecipientsDescription"),
                        text = stringResource(R.string.crypto_recipients_description),
                        textAlign = TextAlign.Start,
                    )
                }
                if (containerRecipientList.isNotEmpty()) {
                    item {
                        Text(
                            modifier =
                                Modifier
                                    .padding(horizontal = SPadding)
                                    .padding(top = SPadding)
                                    .semantics {
                                        heading()
                                        testTagsAsResourceId = true
                                    }.testTag("encryptRecipientsListTitle"),
                            text = stringResource(R.string.crypto_container_added_recipients_title),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                        )
                    }
                    items(containerRecipientList) { recipient ->
                        RecipientItem(
                            recipient = recipient,
                            onClick = { onRecipientClick(it) },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(invisibleElementHeight))
                        if (mainListState.reachedBottom()) {
                            InvisibleElement(modifier = Modifier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordTabContent(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                modifier =
                    Modifier
                        .padding(horizontal = SPadding)
                        .padding(top = SPadding)
                        .semantics {
                            heading()
                            testTagsAsResourceId = true
                        }.testTag("encryptPasswordDescription"),
                text = stringResource(R.string.crypto_password_encryption_description),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun RecipientItem(
    recipient: Addressee,
    isMoreOptionsButtonShown: Boolean = true,
    onClick: (Addressee) -> Unit = {},
) {
    Recipient(
        recipient = recipient,
        isMoreOptionsButtonShown = isMoreOptionsButtonShown,
        onClick = onClick,
    )
    HorizontalDivider(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(SPadding)
                .height(dividerHeight),
    )
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
