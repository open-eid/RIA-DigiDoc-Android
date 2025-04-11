@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.ui.component.crypto.bottombar.CryptoNextBottomBar
import ee.ria.DigiDoc.ui.component.crypto.bottomsheet.CryptoDataFileBottomSheet
import ee.ria.DigiDoc.ui.component.crypto.bottomsheet.EncryptContainerBottomSheet
import ee.ria.DigiDoc.ui.component.crypto.bottomsheet.RecipientBottomSheet
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.EditValueDialog
import ee.ria.DigiDoc.ui.component.shared.ContainerNameView
import ee.ria.DigiDoc.ui.component.shared.CryptoDataFileItem
import ee.ria.DigiDoc.ui.component.shared.CryptoDataFilesLocked
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XLPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.extensions.reachedBottom
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.removeExtensionFromContainerFilename
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.viewmodel.EncryptViewModel
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EncryptNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedRecipientViewModel: SharedRecipientViewModel,
    signingViewModel: SigningViewModel = hiltViewModel(),
    encryptViewModel: EncryptViewModel = hiltViewModel(),
) {
    val cryptoContainer by sharedContainerViewModel.cryptoContainer.asFlow().collectAsState(null)
    val shouldResetContainer by encryptViewModel.shouldResetCryptoContainer.asFlow().collectAsState(false)
    val context = LocalContext.current

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    var isViewInitialized by rememberSaveable { mutableStateOf(false) }

    val clickedFile = remember { mutableStateOf<File?>(null) }
    val clickedRecipient = remember { mutableStateOf<Addressee?>(null) }

    val encryptionAddedSuccess = remember { mutableStateOf(false) }
    val encryptionAddedSuccessText = stringResource(id = R.string.crypto_create_success)

    val emptyFileInContainerText = stringResource(id = R.string.crypto_empty_file_message)

    val showLoadingScreen = remember { mutableStateOf(false) }

    val openRemoveFileDialog = rememberSaveable { mutableStateOf(false) }
    val fileRemoved = stringResource(id = R.string.file_removed)
    val fileRemovalCancelled = stringResource(id = R.string.file_removal_cancelled)
    val closeRemoveFileDialog = {
        openRemoveFileDialog.value = false
    }
    var removeFileDialogMessage =
        stringResource(id = R.string.document_remove_confirmation_message)
    val removeFileCancelButtonContentDescription =
        stringResource(id = R.string.document_cancel_removal_button)
    val removeFileOkButtonContentDescription =
        stringResource(id = R.string.document_confirm_removal_button)
    if ((cryptoContainer?.dataFiles?.size ?: 0) == 1) {
        removeFileDialogMessage =
            stringResource(id = R.string.document_remove_last_confirmation_message)
    }
    val closeContainerMessage = stringResource(id = R.string.crypto_close_container_message)
    val removeContainerMessage = stringResource(id = R.string.remove_container)
    val saveContainerMessage = stringResource(id = R.string.container_save)
    val dismissRemoveFileDialog = {
        closeRemoveFileDialog()
        AccessibilityUtil.sendAccessibilityEvent(
            context,
            TYPE_ANNOUNCEMENT,
            fileRemovalCancelled,
        )
    }

    val containerNameChanged = stringResource(id = R.string.container_name_changed)
    val containerNameChangeCancelled =
        stringResource(
            id = R.string.container_name_change_cancelled,
        )

    val openEditContainerNameDialog = rememberSaveable { mutableStateOf(false) }
    val dismissEditContainerNameDialog = {
        openEditContainerNameDialog.value = false
        AccessibilityUtil.sendAccessibilityEvent(
            context,
            TYPE_ANNOUNCEMENT,
            containerNameChangeCancelled,
        )
    }
    var cryptoContainerName = cryptoContainer?.getName() ?: ""
    var containerName by remember { mutableStateOf(TextFieldValue(text = cryptoContainerName)) }
    val containerExtension = FilenameUtils.getExtension(cryptoContainerName)

    val openRemoveRecipientDialog = rememberSaveable { mutableStateOf(false) }
    val recipientRemoved = stringResource(id = R.string.recipient_removed)
    val recipientRemovalCancelled =
        stringResource(id = R.string.recipient_removal_cancelled)
    val removeRecipientDialogMessage =
        stringResource(id = R.string.crypto_recipient_remove_confirmation_message)
    val removeRecipientCancelButtonContentDescription =
        stringResource(id = R.string.crypto_cancel_recipient_removal_button)
    val removeRecipientOkButtonContentDescription =
        stringResource(id = R.string.crypto_confirm_recipient_removal_button)

    val containerFilesDescription =
        if (encryptViewModel.isEncryptedContainer(cryptoContainer)) {
            stringResource(R.string.crypto_encrypted_documents_title)
        } else {
            stringResource(R.string.crypto_documents_title)
        }

    val closeRecipientDialog = {
        openRemoveRecipientDialog.value = false
    }

    val dismissRemoveRecipientDialog = {
        closeRecipientDialog()
        AccessibilityUtil.sendAccessibilityEvent(
            context,
            TYPE_ANNOUNCEMENT,
            recipientRemovalCancelled,
        )
    }

    val openEncryptionDialog = rememberSaveable { mutableStateOf(false) }
    val encryptionCancelled = stringResource(id = R.string.encryption_cancelled)
    val dismissDialog = {
        openEncryptionDialog.value = false
    }
    val cancelButtonClick = {
        dismissDialog()
        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, encryptionCancelled)
    }

    var recipients by remember { mutableStateOf<List<Addressee>>(emptyList()) }
    val showRecipientsLoadingIndicator = remember { mutableStateOf(false) }
    val recipientsLoading = stringResource(id = R.string.recipients_loading)
    val recipientsLoaded = stringResource(id = R.string.recipients_loaded)

    var dataFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    val showDataFilesLoadingIndicator = remember { mutableStateOf(false) }
    val dataFilesLoading = stringResource(id = R.string.container_files_loading)
    val dataFilesLoaded = stringResource(id = R.string.container_files_loaded)
    val containerFilesLoaded = stringResource(id = R.string.container_files_loaded)

    val listState = rememberLazyListState()

    val showContainerCloseConfirmationDialog = remember { mutableStateOf(false) }

    val showContainerBottomSheet = remember { mutableStateOf(false) }
    val showDataFileBottomSheet = remember { mutableStateOf(false) }
    val showRecipientBottomSheet = remember { mutableStateOf(false) }

    val onDataFileClick: (File) -> Unit = { file ->
        showDataFileBottomSheet.value = true
        clickedFile.value = file
    }

    val onRecipientItemClick: (Addressee) -> Unit = { recipient ->
        showRecipientBottomSheet.value = true
        clickedRecipient.value = recipient
    }

    val onSignActionClick: () -> Unit = {
        showLoadingScreen.value = true
        CoroutineScope(IO).launch {
            encryptViewModel.openSignedContainer(
                context,
                cryptoContainer?.file,
                sharedContainerViewModel,
            )

            delay(2000)
            withContext(Main) {
                navController.navigate(Route.Signing.route) {
                    popUpTo(Route.Home.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
                showLoadingScreen.value = false
            }
        }
    }

    val actionFile by remember { mutableStateOf<File?>(null) }

    var isSaved by remember { mutableStateOf(false) }

    val selectedCryptoContainerTabIndex = rememberSaveable { mutableIntStateOf(0) }

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    actionFile?.let { file ->
                        sharedContainerViewModel
                            .getCryptoContainerDataFile(cryptoContainer, file)
                            ?.let { sharedContainerViewModel.saveContainerFile(it, result) }
                        showMessage(context, R.string.file_saved)
                        isSaved = true
                    } ?: run {
                        cryptoContainer?.file?.let {
                            sharedContainerViewModel.saveContainerFile(it, result)
                            showMessage(context, R.string.file_saved)
                            isSaved = true
                        } ?: showMessage(context, R.string.file_saved_error)
                    }
                } catch (_: Exception) {
                    showMessage(context, R.string.file_saved_error)
                }
            }
        }

    BackHandler {
        showContainerCloseConfirmationDialog.value = true
    }

    DisposableEffect(shouldResetContainer) {
        onDispose {
            if (shouldResetContainer == true) {
                sharedContainerViewModel.resetSignedContainer()
                sharedContainerViewModel.resetCryptoContainer()
            }
        }
    }

    LaunchedEffect(cryptoContainer) {
        cryptoContainer?.let {
            val pastTime = System.currentTimeMillis()
            showRecipientsLoadingIndicator.value = true
            recipients = it.getRecipients()
            showRecipientsLoadingIndicator.value = false
            val newTime = System.currentTimeMillis()
            if (newTime >= (pastTime + 2 * 1000)) {
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, recipientsLoaded)
            }
        }
    }

    LaunchedEffect(cryptoContainer) {
        cryptoContainer?.let {
            val pastTime = System.currentTimeMillis()
            showDataFilesLoadingIndicator.value = true
            dataFiles = it.getDataFiles()
            if (!isViewInitialized) {
                showMessage(containerFilesLoaded)
                isViewInitialized = true
            }
            showDataFilesLoadingIndicator.value = false
            val newTime = System.currentTimeMillis()
            if (newTime >= (pastTime + 2 * 1000)) {
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, dataFilesLoaded)
            }
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            if (showContainerCloseConfirmationDialog.value) {
                showContainerCloseConfirmationDialog.value = false
                handleBackButtonClick(
                    navController,
                    encryptViewModel,
                    sharedContainerViewModel,
                )
            }
            isSaved = false
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
                .testTag("encryptScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = null,
                leftIcon = R.drawable.ic_m3_close_48dp_wght400,
                leftIconContentDescription = R.string.crypto_close_container_title,
                onLeftButtonClick = {
                    if (encryptViewModel.isEncryptedContainer(cryptoContainer)) {
                        showContainerCloseConfirmationDialog.value = true
                    } else {
                        handleBackButtonClick(
                            navController,
                            encryptViewModel,
                            sharedContainerViewModel,
                        )
                    }
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
        bottomBar = {
            CryptoNextBottomBar(
                modifier = modifier,
                onNextClick = {
                    navController.navigate(
                        Route.EncryptRecipientScreen.route,
                    )
                },
                onShareClick = {
                    val containerFile = cryptoContainer?.file
                    if (containerFile != null) {
                        val intent =
                            createContainerAction(
                                context,
                                context.getString(R.string.file_provider_authority),
                                containerFile,
                                encryptViewModel.getMimetype(containerFile) ?: "",
                                Intent.ACTION_SEND,
                            )
                        context.startActivity(intent, null)
                    }
                },
                onAddMoreFiles = {
                    navController.navigate(
                        Route.CryptoFileChoosing.route,
                    )
                },
                isNoRecipientContainer = cryptoContainer?.hasRecipients() == false,
            )
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.primary)
                    .focusGroup()
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("encryptContainer"),
        ) {
            var actionRecipient by remember { mutableStateOf<Addressee?>(null) }

            val showSivaDialog = remember { mutableStateOf(false) }
            val nestedFile = rememberSaveable { mutableStateOf<File?>(null) }

            val openNestedContainer: (nestedContainer: File, isSivaConfirmed: Boolean) -> Unit =
                { nestedContainer, isSivaConfirmed ->
                    CoroutineScope(IO).launch {
                        try {
                            signingViewModel.openNestedContainer(
                                context,
                                nestedContainer,
                                sharedContainerViewModel,
                                isSivaConfirmed,
                            )

                            delay(2000)
                            withContext(Main) {
                                navController.navigate(Route.Signing.route) {
                                    popUpTo(Route.Home.route) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                            showLoadingScreen.value = false
                        } catch (ex: Exception) {
                            withContext(Main) {
                                errorLog(
                                    this.javaClass.simpleName,
                                    "Unable to open nested container",
                                    ex,
                                )
                                showLoadingScreen.value = false
                                Toast.makeText(
                                    context,
                                    ex.localizedMessage,
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        }
                    }
                }

            val handleSivaConfirmation: () -> Unit = {
                showSivaDialog.value = false
                nestedFile.value?.let { file ->
                    openNestedContainer(file, true)
                }
            }

            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(SPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                if (encryptionAddedSuccess.value == true) {
                    showMessage(encryptionAddedSuccessText)
                    encryptionAddedSuccess.value = false
                }

                if (encryptViewModel.isEmptyFileInContainer(cryptoContainer) &&
                    !encryptViewModel.isEncryptedContainer(cryptoContainer)
                ) {
                    showMessage(emptyFileInContainerText)
                }

                LazyColumn(
                    state = listState,
                    modifier = modifier.testTag("lazyColumnScrollView"),
                ) {
                    item {
                        cryptoContainerName = cryptoContainer?.getName() ?: ""
                        containerName =
                            TextFieldValue(
                                text = removeExtensionFromContainerFilename(cryptoContainerName),
                            )
                        cryptoContainer?.let {
                            val isNoRecipientsContainer =
                                !encryptViewModel.isContainerWithoutRecipients(cryptoContainer)
                            val title =
                                if (!isNoRecipientsContainer) {
                                    stringResource(R.string.crypto_new_title)
                                } else {
                                    stringResource(R.string.crypto_view_title)
                                }
                            Text(
                                modifier =
                                    modifier
                                        .padding(bottom = SPadding)
                                        .semantics {
                                            heading()
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("encryptionTitle"),
                                text = title,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Start,
                            )

                            ContainerNameView(
                                icon = R.drawable.ic_m3_stylus_note_48dp_wght400,
                                name = cryptoContainerName,
                                showSignButton =
                                    encryptViewModel.isSignButtonShown(
                                        cryptoContainer,
                                    ),
                                showEncryptButton =
                                    encryptViewModel.isDecryptButtonShown(
                                        cryptoContainer,
                                    ),
                                leftActionButtonName = R.string.signature_update_signature_add,
                                rightActionButtonName = R.string.decrypt_button,
                                leftActionButtonContentDescription = R.string.signature_update_signature_add,
                                rightActionButtonContentDescription = R.string.decrypt_button_accessibility,
                                onLeftActionButtonClick = onSignActionClick,
                                onRightActionButtonClick = {
                                    // TODO: Implement decrypt click
                                },
                                onMoreOptionsActionButtonClick = {
                                    showContainerBottomSheet.value = true
                                },
                            )
                        }
                    }
                    cryptoContainer?.let {
                        if (showDataFilesLoadingIndicator.value) {
                            item {
                                Box(
                                    modifier =
                                        modifier
                                            .fillMaxSize()
                                            .padding(vertical = XLPadding),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier =
                                            modifier
                                                .size(loadingBarSize)
                                                .semantics {
                                                    this.contentDescription = dataFilesLoading
                                                }
                                                .testTag("dataFilesLoadingProgress"),
                                    )
                                }
                            }
                        } else {
                            if (encryptViewModel.isContainerWithoutRecipients(cryptoContainer)) {
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
                                                .testTag("encryptDocumentsTitle"),
                                        text = containerFilesDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Start,
                                    )
                                    CryptoDataFileItem(
                                        modifier = modifier,
                                        dataFiles = dataFiles,
                                        isMoreOptionsButtonShown = true,
                                        onClick = onDataFileClick,
                                    )
                                }
                            } else {
                                item {
                                    TabView(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("encryptionTabView"),
                                        selectedTabIndex = selectedCryptoContainerTabIndex.intValue,
                                        onTabSelected = { index ->
                                            selectedCryptoContainerTabIndex.intValue = index
                                        },
                                        listOf(
                                            Pair(containerFilesDescription) {
                                                if (encryptViewModel
                                                        .shouldShowDataFiles(cryptoContainer)
                                                ) {
                                                    CryptoDataFileItem(
                                                        modifier = modifier,
                                                        dataFiles = dataFiles,
                                                        isMoreOptionsButtonShown =
                                                            encryptViewModel.isDecryptedContainer(
                                                                cryptoContainer,
                                                            ),
                                                        onClick = onDataFileClick,
                                                    )
                                                } else {
                                                    CryptoDataFilesLocked(modifier = modifier)
                                                }
                                            },
                                            Pair(
                                                stringResource(R.string.crypto_container_recipients_title),
                                            ) {
                                                RecipientComponent(
                                                    modifier,
                                                    recipients,
                                                    showRecipientsLoadingIndicator.value,
                                                    recipientsLoading,
                                                    onRecipientItemClick,
                                                )
                                            },
                                        ),
                                    )
                                }
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
            if (openEditContainerNameDialog.value) {
                BasicAlertDialog(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            },
                    onDismissRequest = dismissEditContainerNameDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(XSPadding)
                                .testTag("editContainerNameDialog"),
                    ) {
                        EditValueDialog(
                            modifier = modifier,
                            title = stringResource(id = R.string.crypto_containter_update_name),
                            subtitle = stringResource(id = R.string.crypto_containter_update_name),
                            editValue = containerName,
                            onEditValueChange = {
                                containerName = it
                            },
                            onClearValueClick = {
                                containerName = TextFieldValue("")
                            },
                            cancelButtonClick = dismissEditContainerNameDialog,
                            okButtonClick = {
                                CoroutineScope(IO).launch {
                                    cryptoContainer?.setName(
                                        "${containerName.text}.$containerExtension",
                                    )
                                }
                                openEditContainerNameDialog.value = false
                                AccessibilityUtil.sendAccessibilityEvent(
                                    context,
                                    TYPE_ANNOUNCEMENT,
                                    containerNameChanged,
                                )
                            },
                        )
                        InvisibleElement(modifier = modifier)
                    }
                }
            }

            if (openRemoveFileDialog.value) {
                BasicAlertDialog(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            },
                    onDismissRequest = dismissRemoveFileDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .padding(screenViewLargePadding)
                                .verticalScroll(rememberScrollState())
                                .testTag("documentRemovalDialog"),
                    ) {
                        MessageDialog(
                            modifier = modifier,
                            title = stringResource(R.string.main_menu_remove_file),
                            message = removeFileDialogMessage,
                            showIcons = false,
                            dismissButtonText = stringResource(R.string.cancel_button),
                            confirmButtonText = stringResource(R.string.remove_title),
                            dismissButtonContentDescription = removeFileCancelButtonContentDescription,
                            confirmButtonContentDescription = removeFileOkButtonContentDescription,
                            onDismissRequest = dismissRemoveFileDialog,
                            onDismissButton = dismissRemoveFileDialog,
                            onConfirmButton = {
                                if ((cryptoContainer?.dataFiles?.size ?: 0) == 1) {
                                    cryptoContainer?.file?.delete()
                                    sharedContainerViewModel.resetCryptoContainer()
                                    handleBackButtonClick(
                                        navController,
                                        encryptViewModel,
                                        sharedContainerViewModel,
                                    )
                                } else {
                                    CoroutineScope(IO).launch {
                                        try {
                                            sharedContainerViewModel.removeCryptoContainerDataFile(
                                                cryptoContainer,
                                                clickedFile.value,
                                            )
                                        } catch (_: Exception) {
                                            withContext(Main) {
                                                showMessage(context, R.string.error_general_client)
                                            }
                                        }
                                    }
                                }
                                closeRemoveFileDialog()
                                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, fileRemoved)
                            },
                        )
                        InvisibleElement(modifier = modifier)
                    }
                }
            }
            if (openRemoveRecipientDialog.value) {
                BasicAlertDialog(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            },
                    onDismissRequest = dismissRemoveRecipientDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = screenViewLargePadding)
                                .testTag("recipientRemovalDialog"),
                    ) {
                        MessageDialog(
                            modifier = modifier.testTag("dialogText"),
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
                                    sharedContainerViewModel.removeRecipient(
                                        cryptoContainer,
                                        actionRecipient,
                                    )
                                }
                                closeRecipientDialog()
                                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, recipientRemoved)
                            },
                        )
                        InvisibleElement(modifier = modifier)
                    }
                }
            }

            CryptoDataFileBottomSheet(
                modifier = modifier,
                showSheet = showDataFileBottomSheet.value,
                nestedFile = nestedFile,
                onDataFileBottomSheetDismiss = {
                    showDataFileBottomSheet.value = false
                },
                clickedDataFile = clickedFile,
                cryptoContainer = cryptoContainer,
                sharedContainerViewModel = sharedContainerViewModel,
                encryptViewModel = encryptViewModel,
                showLoadingScreen = showLoadingScreen,
                showSivaDialog = showSivaDialog,
                handleSivaConfirmation = handleSivaConfirmation,
                context = context,
                saveFileLauncher = saveFileLauncher,
                saveFile = ::saveFile,
                openRemoveFileDialog = openRemoveFileDialog,
                onBackButtonClick = {
                    handleBackButtonClick(
                        navController,
                        encryptViewModel,
                        sharedContainerViewModel,
                    )
                },
            )

            EncryptContainerBottomSheet(
                modifier = modifier,
                showSheet = showContainerBottomSheet,
                isEditContainerButtonShown = cryptoContainer?.hasRecipients() == false,
                openEditContainerNameDialog = openEditContainerNameDialog,
                isSignButtonShown = cryptoContainer?.encrypted == true,
                cryptoContainer = cryptoContainer,
                onSignClick = onSignActionClick,
                saveFileLauncher = saveFileLauncher,
                saveFile = ::saveFile,
            )

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

            if (showLoadingScreen.value) {
                LoadingScreen(modifier = modifier)
            }

            if (showContainerCloseConfirmationDialog.value) {
                MessageDialog(
                    modifier = modifier,
                    title = stringResource(R.string.crypto_close_container_title),
                    message = closeContainerMessage,
                    showIcons = true,
                    dismissIcon = R.drawable.ic_m3_download_48dp_wght400,
                    confirmIcon = R.drawable.ic_m3_delete_48dp_wght400,
                    dismissButtonText = stringResource(R.string.save),
                    confirmButtonText = stringResource(R.string.remove_title),
                    dismissButtonContentDescription = saveContainerMessage,
                    confirmButtonContentDescription = removeContainerMessage,
                    onDismissRequest = {
                        showContainerCloseConfirmationDialog.value = false
                    },
                    onDismissButton = {
                        saveFile(
                            cryptoContainer?.file,
                            cryptoContainer?.containerMimetype(),
                            saveFileLauncher,
                        )
                    },
                    onConfirmButton = {
                        showContainerCloseConfirmationDialog.value = false
                        val containerFile = cryptoContainer?.file
                        if (containerFile?.exists() == true) {
                            containerFile.delete()
                        }
                        sharedContainerViewModel.resetCryptoContainer()
                        handleBackButtonClick(navController, encryptViewModel, sharedContainerViewModel)
                    },
                )
            }
        }
    }
}

fun handleBackButtonClick(
    navController: NavHostController,
    encryptViewModel: EncryptViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    sharedContainerViewModel.clearContainers()
    encryptViewModel.handleBackButton()
    navController.navigateUp()
}

private fun saveFile(
    file: File?,
    mimetype: String?,
    saveFileLauncher: ActivityResultLauncher<Intent>,
) {
    try {
        val saveIntent =
            Intent.createChooser(
                Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(
                        Intent.EXTRA_TITLE,
                        sanitizeString(file?.name, ""),
                    )
                    .setType(mimetype)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                null,
            )
        saveFileLauncher.launch(saveIntent)
    } catch (_: ActivityNotFoundException) {
        // No activity to handle this kind of files
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptNavigationPreview() {
    RIADigiDocTheme {
        EncryptNavigation(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedContainerViewModel = hiltViewModel(),
            sharedRecipientViewModel = hiltViewModel(),
        )
    }
}
