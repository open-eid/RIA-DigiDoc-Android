@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.pluralStringResource
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
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.model.notifications.ContainerNotificationType
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.EditValueDialog
import ee.ria.DigiDoc.ui.component.shared.ContainerNameView
import ee.ria.DigiDoc.ui.component.shared.DataFileItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.shared.dialog.SivaConfirmationDialog
import ee.ria.DigiDoc.ui.component.signing.bottombar.SigningBottomBar
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.ContainerBottomSheet
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.DataFileBottomSheet
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.SignatureBottomSheet
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.SignedContainerBottomSheet
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.extensions.reachedBottom
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.removeExtensionFromContainerFilename
import ee.ria.DigiDoc.utilsLib.extensions.isContainer
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.viewmodel.EncryptViewModel
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SigningNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSignatureViewModel: SharedSignatureViewModel,
    signingViewModel: SigningViewModel = hiltViewModel(),
    encryptViewModel: EncryptViewModel = hiltViewModel(),
) {
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val shouldResetContainer by signingViewModel.shouldResetSignedContainer.asFlow().collectAsState(false)
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val signedContainerExists = signedContainer?.getContainerFile()?.exists()

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val clickedDataFile = remember { mutableStateOf<DataFileInterface?>(null) }
    val clickedSignature = remember { mutableStateOf<SignatureInterface?>(null) }

    val signatureAddedSuccess = remember { mutableStateOf(false) }
    val signatureAddedSuccessText = stringResource(id = R.string.signature_update_signature_add_success)

    val isNestedContainer = sharedContainerViewModel.isNestedContainer(signedContainer)
    val isXadesContainer = signedContainer?.isXades() == true
    val isCadesContainer = signedContainer?.isCades() == true

    var validSignaturesCount by remember { mutableIntStateOf(0) }
    var unknownSignaturesCount by remember { mutableIntStateOf(0) }
    var invalidSignaturesCount by remember { mutableIntStateOf(0) }

    var isSignaturesCountLoaded by remember { mutableStateOf(false) }

    val isParentContainerSivaConfirmed = sharedContainerViewModel.isSivaConfirmed.value == true
    var isSivaConfirmed by remember { mutableStateOf(isParentContainerSivaConfirmed) }
    var isTimestampedContainer by remember { mutableStateOf(false) }

    val containerHasText = stringResource(id = R.string.container_has)
    val validSignaturesText =
        pluralStringResource(id = R.plurals.signatures_valid, count = validSignaturesCount, validSignaturesCount)
    val unknownSignaturesText =
        pluralStringResource(id = R.plurals.signatures_unknown, count = unknownSignaturesCount, unknownSignaturesCount)
    val invalidSignaturesText =
        pluralStringResource(id = R.plurals.signatures_invalid, count = invalidSignaturesCount, invalidSignaturesCount)

    val showLoadingScreen = remember { mutableStateOf(false) }

    val openRemoveFileDialog = rememberSaveable { mutableStateOf(false) }
    val fileRemoved = stringResource(id = R.string.document_removed)
    val fileRemovalCancelled = stringResource(id = R.string.document_removal_cancelled)
    val closeRemoveFileDialog = {
        openRemoveFileDialog.value = false
    }
    var removeFileDialogMessage =
        stringResource(id = R.string.document_remove_confirmation_message)
    val removeFileCancelButtonContentDescription =
        stringResource(id = R.string.document_cancel_removal_button)
    val removeFileOkButtonContentDescription =
        stringResource(id = R.string.document_confirm_removal_button)
    if ((signedContainer?.rawContainer()?.dataFiles()?.size ?: 0) == 1) {
        removeFileDialogMessage =
            stringResource(id = R.string.document_remove_last_confirmation_message)
    }
    val closeContainerMessage = stringResource(id = R.string.signing_close_container_message)
    val removeContainerMessage = stringResource(id = R.string.remove_container)
    val saveContainerMessage = stringResource(id = R.string.container_save)
    val dismissRemoveFileDialog = {
        closeRemoveFileDialog()
        sendAccessibilityEvent(
            context,
            getAccessibilityEventType(),
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
        sendAccessibilityEvent(
            context,
            getAccessibilityEventType(),
            containerNameChangeCancelled,
        )
    }
    var signedContainerName = signedContainer?.getName() ?: ""
    var containerName by remember { mutableStateOf(TextFieldValue(text = signedContainerName)) }
    val containerExtension = FilenameUtils.getExtension(signedContainerName)

    val openRemoveSignatureDialog = rememberSaveable { mutableStateOf(false) }
    val signatureRemoved = stringResource(id = R.string.signature_removed)
    val signatureRemovalCancelled =
        stringResource(id = R.string.signature_removal_cancelled)
    val removeSignatureDialogMessage =
        stringResource(id = R.string.signature_update_signature_remove_confirmation_message)
    val removeSignatureCancelButtonContentDescription =
        stringResource(id = R.string.signature_update_cancel_signature_removal_button)
    val removeSignatureOkButtonContentDescription =
        stringResource(id = R.string.signature_update_confirm_signature_removal_button)

    val closeSignatureDialog = {
        openRemoveSignatureDialog.value = false
    }

    val dismissRemoveSignatureDialog = {
        closeSignatureDialog()
        sendAccessibilityEvent(
            context,
            getAccessibilityEventType(),
            signatureRemovalCancelled,
        )
    }

    val openSignatureDialog = rememberSaveable { mutableStateOf(false) }
    val signingCancelled = stringResource(id = R.string.signing_cancelled)
    val dismissDialog = {
        openSignatureDialog.value = false
    }
    val cancelButtonClick = {
        dismissDialog()
        sendAccessibilityEvent(context, getAccessibilityEventType(), signingCancelled)
    }

    var signatures by remember { mutableStateOf<List<SignatureInterface>>(emptyList()) }
    val showSignaturesLoadingIndicator = remember { mutableStateOf(false) }
    val signaturesLoading = stringResource(id = R.string.signatures_loading)
    val signaturesLoaded = stringResource(id = R.string.signatures_loaded)

    var dataFiles by remember { mutableStateOf<List<DataFileInterface>>(emptyList()) }
    val showDataFilesLoadingIndicator = remember { mutableStateOf(false) }
    val dataFilesLoading = stringResource(id = R.string.container_files_loading)

    val filesAdded by sharedContainerViewModel.addedFilesCount.collectAsState(0)

    val listState = rememberLazyListState()

    val showContainerCloseConfirmationDialog = remember { mutableStateOf(false) }

    val showContainerBottomSheet = remember { mutableStateOf(false) }
    val showSignedContainerBottomSheet = remember { mutableStateOf(false) }
    val showDataFileBottomSheet = remember { mutableStateOf(false) }
    val showSignatureBottomSheet = remember { mutableStateOf(false) }

    val onDataFileClick: (DataFileInterface) -> Unit = { dataFile ->
        showDataFileBottomSheet.value = true
        clickedDataFile.value = dataFile
    }

    val onSignatureMoreClick: (SignatureInterface) -> Unit = { signature ->
        showSignatureBottomSheet.value = true
        clickedSignature.value = signature
    }

    val onEncryptActionClick: () -> Unit = {
        showLoadingScreen.value = true
        scope.launch(IO) {
            try {
                signingViewModel.openCryptoContainer(
                    context,
                    signedContainer,
                    sharedContainerViewModel,
                )

                withContext(Main) {
                    showMessage(context, R.string.converted_to_crypto_container)
                }

                delay(2000)
                withContext(Main) {
                    navController.navigate(Route.Encrypt.route) {
                        popUpTo(Route.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    showLoadingScreen.value = true
                }
            } catch (_: Exception) {
                showMessage(context, R.string.container_load_error)
            }
        }
    }

    var isSaved by remember { mutableStateOf(false) }

    val selectedSignedContainerTabIndex = rememberSaveable { mutableIntStateOf(0) }

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val containerNotifications by sharedContainerViewModel.containerNotifications.collectAsState()

    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    clickedDataFile.value?.let { datafile ->
                        sharedContainerViewModel
                            .getContainerDataFile(signedContainer, datafile)
                            ?.let { sharedContainerViewModel.saveContainerFile(it, result) }
                        showMessage(context, R.string.file_saved)
                        clickedDataFile.value = null
                        isSaved = true
                    } ?: run {
                        signedContainer?.getContainerFile()?.let {
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
        if (!isNestedContainer) {
            showContainerCloseConfirmationDialog.value = true
        } else {
            handleBackButtonClick(
                navController,
                signingViewModel,
                sharedContainerViewModel,
            )
        }
    }

    DisposableEffect(shouldResetContainer) {
        onDispose {
            if (shouldResetContainer == true) {
                sharedContainerViewModel.resetSignedContainer()
                sharedContainerViewModel.resetCryptoContainer()
                sharedContainerViewModel.resetContainerNotifications()
            }
        }
    }

    LaunchedEffect(Unit) {
        sharedContainerViewModel.setSignedContainer(sharedContainerViewModel.currentContainer() as? SignedContainer)
    }

    LaunchedEffect(sharedContainerViewModel.signedMidStatus) {
        sharedContainerViewModel.signedMidStatus.asFlow().collect { status ->
            status?.let {
                if (status == MobileCreateSignatureProcessStatus.OK) {
                    signatures = signedContainer?.getSignatures() ?: emptyList()
                    withContext(Main) {
                        selectedSignedContainerTabIndex.intValue = 1
                        signatureAddedSuccess.value = true
                        sendAccessibilityEvent(context, getAccessibilityEventType(), signatureAddedSuccessText)
                        delay(3000)
                        signatureAddedSuccess.value = false
                        sharedContainerViewModel.setSignedMidStatus(null)
                    }
                }
            }
        }
    }

    LaunchedEffect(sharedContainerViewModel.signedSidStatus) {
        sharedContainerViewModel.signedSidStatus.asFlow().collect { status ->
            status?.let {
                if (status == SessionStatusResponseProcessStatus.OK) {
                    signatures = signedContainer?.getSignatures() ?: emptyList()
                    withContext(Main) {
                        signatureAddedSuccess.value = true
                        selectedSignedContainerTabIndex.intValue = 1
                        sendAccessibilityEvent(context, getAccessibilityEventType(), signatureAddedSuccessText)
                        delay(5000)
                        signatureAddedSuccess.value = false
                        sharedContainerViewModel.setSignedSidStatus(null)
                    }
                }
            }
        }
    }

    LaunchedEffect(sharedContainerViewModel.signedNFCStatus) {
        sharedContainerViewModel.signedNFCStatus.asFlow().collect { status ->
            status?.let {
                if (status == true) {
                    signatures = signedContainer?.getSignatures() ?: emptyList()
                    withContext(Main) {
                        signatureAddedSuccess.value = true
                        selectedSignedContainerTabIndex.intValue = 1
                        sendAccessibilityEvent(context, getAccessibilityEventType(), signatureAddedSuccessText)
                        delay(5000)
                        signatureAddedSuccess.value = false
                        sharedContainerViewModel.setSignedNFCStatus(null)
                    }
                }
            }
        }
    }

    LaunchedEffect(sharedContainerViewModel.signedIDCardStatus) {
        sharedContainerViewModel.signedIDCardStatus.asFlow().collect { status ->
            status?.let {
                if (status == true) {
                    signatures = signedContainer?.getSignatures() ?: emptyList()
                    withContext(Main) {
                        signatureAddedSuccess.value = true
                        selectedSignedContainerTabIndex.intValue = 1
                        sendAccessibilityEvent(context, getAccessibilityEventType(), signatureAddedSuccessText)
                        delay(5000)
                        signatureAddedSuccess.value = false
                        sharedContainerViewModel.setSignedIDCardStatus(null)
                    }
                }
            }
        }
    }

    LaunchedEffect(signedContainer) {
        signedContainer?.let {
            val pastTime = System.currentTimeMillis()
            showSignaturesLoadingIndicator.value = true
            signatures = it.getSignatures()
            showSignaturesLoadingIndicator.value = false
            withContext(Main) {
                val signatureCounts = signedContainer?.getSignaturesStatusCount()
                validSignaturesCount = signatureCounts?.get(ValidatorInterface.Status.Valid) ?: 0
                unknownSignaturesCount =
                    signatureCounts?.get(ValidatorInterface.Status.Unknown) ?: 0
                invalidSignaturesCount =
                    signatureCounts?.get(ValidatorInterface.Status.Invalid) ?: 0

                sharedContainerViewModel.setContainerNotifications(
                    listOfNotNull(
                        ContainerNotificationType.XadesFile.takeIf { isXadesContainer },
                        ContainerNotificationType.CadesFile.takeIf { isCadesContainer },
                        ContainerNotificationType
                            .UnknownSignatures(
                                unknownSignaturesCount,
                            ).takeIf { unknownSignaturesCount > 0 },
                        ContainerNotificationType
                            .InvalidSignatures(
                                invalidSignaturesCount,
                            ).takeIf { invalidSignaturesCount > 0 },
                    ),
                )
            }
            val newTime = System.currentTimeMillis()
            if (newTime >= (pastTime + 2 * 1000)) {
                sendAccessibilityEvent(context, getAccessibilityEventType(), signaturesLoaded)
            }
        }
    }

    LaunchedEffect(signedContainer, validSignaturesCount, unknownSignaturesCount, invalidSignaturesCount) {
        signedContainer?.let {
            if (!isSignaturesCountLoaded && signatures.isNotEmpty()) {
                val announcementText =
                    when {
                        unknownSignaturesCount == 0 && invalidSignaturesCount == 0 -> {
                            validSignaturesCount = signatures.size
                            "$containerHasText, ${validSignaturesText.lowercase()}"
                        }
                        else ->
                            buildString {
                                append(containerHasText)
                                if (unknownSignaturesCount > 0) append(" ${unknownSignaturesText.lowercase()}")
                                if (invalidSignaturesCount > 0) append(" ${invalidSignaturesText.lowercase()}")
                            }
                    }

                delay(1000)
                isSignaturesCountLoaded = true
                sendAccessibilityEvent(
                    context,
                    getAccessibilityEventType(),
                    announcementText,
                )
            }
        }
    }

    LaunchedEffect(signedContainer) {
        signedContainer?.let {
            showDataFilesLoadingIndicator.value = true
            dataFiles = it.getDataFiles()
            showDataFilesLoadingIndicator.value = false
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            if (showContainerCloseConfirmationDialog.value) {
                showContainerCloseConfirmationDialog.value = false
                handleBackButtonClick(
                    navController,
                    signingViewModel,
                    sharedContainerViewModel,
                )
            }
            isSaved = false
        }
    }

    LaunchedEffect(signedContainer, isSivaConfirmed) {
        signedContainer?.let { container ->
            scope.launch(IO) {
                isTimestampedContainer =
                    signingViewModel.isTimestampedContainer(
                        container,
                        isSivaConfirmed,
                    )
            }
        }
    }

    LaunchedEffect(filesAdded) {
        when {
            filesAdded == 1 -> showMessage(context, R.string.file_added)
            filesAdded > 1 -> showMessage(context, R.string.files_added)
        }
        sharedContainerViewModel.resetAddedFilesCount()
    }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(
                    message = message,
                    withDismissAction = true,
                )
            }
            SnackBarManager.removeMessage(message)
        }
    }

    if (signedContainerExists == false) {
        return
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
                }.testTag("signingScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title =
                    signedContainer
                        ?.takeIf { it.isSigned() }
                        ?.let { R.string.signing_container_documents_title },
                leftIcon =
                    when {
                        isNestedContainer -> R.drawable.ic_m3_arrow_back_48dp_wght400
                        else -> R.drawable.ic_m3_close_48dp_wght400
                    },
                leftIconContentDescription = R.string.signing_close_container_title,
                onLeftButtonClick = {
                    if (!isNestedContainer) {
                        showContainerCloseConfirmationDialog.value = true
                    } else {
                        handleBackButtonClick(
                            navController,
                            signingViewModel,
                            sharedContainerViewModel,
                        )
                    }
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
                showExtraButton = containerNotifications.isNotEmpty(),
                extraButtonItemCount = containerNotifications.size,
                onExtraButtonClick = {
                    navController.navigate(
                        Route.ContainerNotificationsScreen.route,
                    )
                },
            )
        },
        bottomBar = {
            SigningBottomBar(
                modifier = modifier,
                onSignClick = {
                    navController.navigate(
                        Route.SignatureInputScreen.route,
                    )
                },
                onShareClick = {
                    val containerFile = signedContainer?.getContainerFile()
                    if (containerFile != null) {
                        val intent =
                            createContainerAction(
                                context,
                                context.getString(R.string.file_provider_authority),
                                containerFile,
                                signingViewModel.getMimetype(containerFile) ?: "",
                                Intent.ACTION_SEND,
                            )
                        context.startActivity(intent, null)
                    }
                },
                onAddMoreFiles = {
                    navController.navigate(
                        Route.FileChoosing.route,
                    )
                },
                isUnsignedContainer = !isNestedContainer && signedContainer?.isSigned() == false,
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
                    }.testTag("signingContainer"),
        ) {
            var actionSignature by remember { mutableStateOf<SignatureInterface?>(null) }

            val showSivaDialog = remember { mutableStateOf(false) }
            val nestedFile = rememberSaveable { mutableStateOf<File?>(null) }

            val openNestedContainer: (nestedContainer: File, isSivaConfirmed: Boolean) -> Unit =
                { nestedContainer, isSivaConfirmed ->
                    scope.launch(IO) {
                        try {
                            val isSigningContainer = nestedContainer.isContainer(context)
                            if (!isSigningContainer) {
                                encryptViewModel.openNestedContainer(
                                    context,
                                    nestedContainer,
                                    sharedContainerViewModel,
                                )

                                withContext(Main) {
                                    navController.navigate(Route.Encrypt.route)
                                }
                            } else {
                                signingViewModel.openNestedContainer(
                                    context,
                                    nestedContainer,
                                    sharedContainerViewModel,
                                    isSivaConfirmed,
                                )
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
                                Toast
                                    .makeText(
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
                isSivaConfirmed = true
                nestedFile.value?.let { file ->
                    openNestedContainer(file, true)
                }
            }

            val handleSivaCancel: () -> Unit = {
                showSivaDialog.value = false
                isSivaConfirmed = false
                nestedFile.value?.let { file ->
                    if (DDOC_MIMETYPE != file.mimeType(context)) {
                        openNestedContainer(file, false)
                    }
                }
            }

            val handleResult: (Boolean) -> Unit = { isSivaConfirmed ->
                if (isSivaConfirmed) {
                    handleSivaConfirmation()
                } else {
                    handleSivaCancel()
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
                if (signatureAddedSuccess.value == true) {
                    showMessage(signatureAddedSuccessText)
                    signatureAddedSuccess.value = false
                }

                LazyColumn(
                    state = listState,
                    modifier = modifier.testTag("lazyColumnScrollView"),
                ) {
                    item {
                        signedContainerName = signedContainer?.getName() ?: ""
                        containerName =
                            TextFieldValue(
                                text = removeExtensionFromContainerFilename(signedContainerName),
                            )
                        signedContainer?.let {
                            val isSignedContainer =
                                !signingViewModel.isContainerWithoutSignatures(signedContainer) &&
                                    !isNestedContainer
                            if (!isSignedContainer) {
                                Text(
                                    modifier =
                                        modifier
                                            .padding(bottom = SPadding)
                                            .semantics {
                                                heading()
                                                testTagsAsResourceId = true
                                            }.testTag("signingTitle"),
                                    text = stringResource(R.string.signature_update_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Start,
                                )
                            }
                            ContainerNameView(
                                icon = R.drawable.ic_m3_stylus_note_48dp_wght400,
                                name = signedContainerName,
                                showLeftActionButton =
                                    signedContainer?.isSigned() == true &&
                                        signingViewModel.isSignButtonShown(
                                            signedContainer,
                                            isNestedContainer,
                                            isXadesContainer,
                                            isCadesContainer,
                                        ),
                                showRightActionButton =
                                    signingViewModel.isEncryptButtonShown(
                                        signedContainer,
                                        isNestedContainer,
                                    ),
                                leftActionButtonName = R.string.signature_update_signature_add,
                                rightActionButtonName = R.string.encrypt_button,
                                leftActionButtonContentDescription = R.string.signature_update_signature_add,
                                rightActionButtonContentDescription = R.string.encrypt_button_accessibility,
                                onLeftActionButtonClick = {
                                    navController.navigate(
                                        Route.SignatureInputScreen.route,
                                    )
                                },
                                onRightActionButtonClick = onEncryptActionClick,
                                onMoreOptionsActionButtonClick = {
                                    showContainerBottomSheet.value = true
                                },
                            )
                        }
                    }
                    signedContainer?.let {
                        if (showDataFilesLoadingIndicator.value) {
                            item {
                                Box(
                                    modifier =
                                        modifier
                                            .fillMaxSize()
                                            .padding(vertical = MPadding),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier =
                                            modifier
                                                .size(loadingBarSize)
                                                .semantics {
                                                    this.contentDescription = dataFilesLoading
                                                }.testTag("dataFilesLoadingProgress"),
                                    )
                                }
                            }
                        } else {
                            if (signingViewModel.isContainerWithoutSignatures(signedContainer) && !isNestedContainer) {
                                item {
                                    Text(
                                        modifier =
                                            modifier
                                                .padding(horizontal = SPadding)
                                                .padding(top = SPadding)
                                                .semantics {
                                                    heading()
                                                    testTagsAsResourceId = true
                                                }.testTag("signingDocumentsTitle"),
                                        text = stringResource(R.string.signing_documents_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Start,
                                    )
                                    DataFileItem(modifier, dataFiles, onDataFileClick)
                                }
                            } else {
                                item {
                                    TabView(
                                        modifier = modifier,
                                        testTag = "signingTabView",
                                        selectedTabIndex = selectedSignedContainerTabIndex.intValue,
                                        onTabSelected = { index -> selectedSignedContainerTabIndex.intValue = index },
                                        listOf(
                                            Pair(
                                                stringResource(R.string.signing_documents_title),
                                            ) {
                                                DataFileItem(
                                                    modifier,
                                                    dataFiles,
                                                    onDataFileClick,
                                                )
                                            },
                                            Pair(
                                                stringResource(R.string.signing_container_signatures_title),
                                            ) {
                                                Column {
                                                    if (signingViewModel.isContainerWithTimestamps(
                                                            signedContainer,
                                                        )
                                                    ) {
                                                        signedContainer?.let { container ->
                                                            container
                                                                .getTimestamps()
                                                                ?.let { timestamps ->
                                                                    Row {
                                                                        SignatureComponent(
                                                                            modifier,
                                                                            true,
                                                                            timestamps,
                                                                            showSignaturesLoadingIndicator.value,
                                                                            signaturesLoading,
                                                                            true,
                                                                            false,
                                                                            onSignatureMoreClick,
                                                                            onSignatureMoreClick,
                                                                        )
                                                                    }
                                                                }
                                                        }
                                                    }

                                                    val timestamps = signedContainer?.getTimestamps()
                                                    val firstTimestamp = timestamps?.firstOrNull()
                                                    val isDdoc = signedContainer?.containerMimetype() == DDOC_MIMETYPE
                                                    val isValid =
                                                        firstTimestamp
                                                            ?.let {
                                                                SignatureStatusUtil.isDdocSignatureValid(it)
                                                            } == true

                                                    Row {
                                                        SignatureComponent(
                                                            modifier,
                                                            isTimestampedContainer,
                                                            signatures,
                                                            showSignaturesLoadingIndicator.value,
                                                            signaturesLoading,
                                                            isTimestampedContainer ||
                                                                signedContainer
                                                                    ?.containerMimetype() == ASICS_MIMETYPE &&
                                                                signatures.size == 1,
                                                            !timestamps.isNullOrEmpty() && isDdoc && isValid,
                                                            onSignatureMoreClick,
                                                            onSignatureMoreClick,
                                                        )
                                                    }
                                                }
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
                            title = stringResource(id = R.string.signature_update_name_update_name),
                            subtitle = stringResource(id = R.string.signature_update_name_update_name),
                            editValue = containerName,
                            onEditValueChange = {
                                containerName = it
                            },
                            onClearValueClick = {
                                containerName = TextFieldValue("")
                            },
                            cancelButtonClick = dismissEditContainerNameDialog,
                            okButtonClick = {
                                scope.launch(IO) {
                                    signedContainer?.setName(
                                        "${containerName.text}.$containerExtension",
                                    )
                                }
                                openEditContainerNameDialog.value = false
                                sendAccessibilityEvent(
                                    context,
                                    getAccessibilityEventType(),
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
                                .padding(SPadding)
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
                                if ((signedContainer?.rawContainer()?.dataFiles()?.size ?: 0) == 1) {
                                    signedContainer?.getContainerFile()?.delete()
                                    sharedContainerViewModel.resetSignedContainer()
                                    sharedContainerViewModel.resetContainerNotifications()
                                    handleBackButtonClick(navController, signingViewModel, sharedContainerViewModel)
                                } else {
                                    scope.launch(IO) {
                                        try {
                                            sharedContainerViewModel.removeContainerDataFile(
                                                signedContainer,
                                                clickedDataFile.value,
                                            )
                                        } catch (_: Exception) {
                                            withContext(Main) {
                                                showMessage(context, R.string.error_general_client)
                                            }
                                        }
                                    }
                                }
                                closeRemoveFileDialog()
                                sendAccessibilityEvent(context, getAccessibilityEventType(), fileRemoved)
                            },
                        )
                        InvisibleElement(modifier = modifier)
                    }
                }
            }
            if (openRemoveSignatureDialog.value) {
                BasicAlertDialog(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            },
                    onDismissRequest = dismissRemoveSignatureDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = SPadding)
                                .testTag("signatureRemovalDialog"),
                    ) {
                        MessageDialog(
                            modifier = modifier.testTag("dialogText"),
                            title = stringResource(R.string.signature_remove_button),
                            message = removeSignatureDialogMessage,
                            showIcons = false,
                            dismissButtonText = stringResource(R.string.cancel_button),
                            confirmButtonText = stringResource(R.string.remove_title),
                            dismissButtonContentDescription = removeSignatureCancelButtonContentDescription,
                            confirmButtonContentDescription = removeSignatureOkButtonContentDescription,
                            onDismissRequest = dismissRemoveSignatureDialog,
                            onDismissButton = dismissRemoveSignatureDialog,
                            onConfirmButton = {
                                scope.launch(IO) {
                                    sharedContainerViewModel.removeSignature(
                                        signedContainer,
                                        actionSignature,
                                    )
                                }
                                closeSignatureDialog()
                                sendAccessibilityEvent(context, getAccessibilityEventType(), signatureRemoved)
                            },
                        )
                        InvisibleElement(modifier = modifier)
                    }
                }
            }

            SivaConfirmationDialog(
                showDialog = showSivaDialog,
                modifier = modifier,
                onResult = handleResult,
            )

            DataFileBottomSheet(
                modifier = modifier,
                showSheet = showDataFileBottomSheet.value,
                nestedFile = nestedFile,
                onDataFileBottomSheetDismiss = {
                    showDataFileBottomSheet.value = false
                },
                clickedDataFile = clickedDataFile,
                signedContainer = signedContainer,
                sharedContainerViewModel = sharedContainerViewModel,
                signingViewModel = signingViewModel,
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
                        signingViewModel,
                        sharedContainerViewModel,
                    )
                },
            )

            ContainerBottomSheet(
                modifier = modifier,
                showSheet = showContainerBottomSheet,
                isEditContainerButtonShown = signedContainer?.isSigned() == false,
                openEditContainerNameDialog = openEditContainerNameDialog,
                isEncryptButtonShown = signedContainer?.isSigned() == false,
                signedContainer = signedContainer,
                onEncryptClick = onEncryptActionClick,
                saveFileLauncher = saveFileLauncher,
                saveFile = ::saveFile,
            )

            SignatureBottomSheet(
                modifier = modifier,
                showSheet = showSignatureBottomSheet,
                clickedSignature = clickedSignature,
                signedContainer = signedContainer,
                signingViewModel = signingViewModel,
                sharedSignatureViewModel = sharedSignatureViewModel,
                navController = navController,
                isNestedContainer = isNestedContainer,
                isXadesContainer = isXadesContainer,
                isCadesContainer = isCadesContainer,
                openRemoveSignatureDialog = openRemoveSignatureDialog,
                onSignatureRemove = { actionSignature = it },
            )

            SignedContainerBottomSheet(
                modifier = modifier,
                showSheet = showSignedContainerBottomSheet,
                signedContainer = signedContainer,
                isNestedContainer = isNestedContainer,
                isXadesContainer = isXadesContainer,
                isCadesContainer = isCadesContainer,
                signingViewModel = signingViewModel,
                navController = navController,
                onEncryptClick = onEncryptActionClick,
                onExtendSignatureClick = {
                    // TODO: Implement extend signature click
                },
            )

            if (showLoadingScreen.value) {
                LoadingScreen(modifier = modifier)
            }

            if (showContainerCloseConfirmationDialog.value) {
                MessageDialog(
                    modifier = modifier,
                    title = stringResource(R.string.signing_close_container_title),
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
                            signedContainer?.getContainerFile(),
                            signedContainer?.containerMimetype(),
                            saveFileLauncher,
                        )
                    },
                    onConfirmButton = {
                        showContainerCloseConfirmationDialog.value = false
                        val containerFile = signedContainer?.getContainerFile()
                        if (containerFile?.exists() == true) {
                            containerFile.delete()
                        }
                        sharedContainerViewModel.resetSignedContainer()
                        sharedContainerViewModel.resetContainerNotifications()
                        handleBackButtonClick(navController, signingViewModel, sharedContainerViewModel)
                    },
                )
            }
        }
    }
}

private fun handleBackButtonClick(
    navController: NavHostController,
    signingViewModel: SigningViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    sharedContainerViewModel.resetExternalFileUris()
    sharedContainerViewModel.resetIsSivaConfirmed()
    if (sharedContainerViewModel.nestedContainers.size > 1) {
        sharedContainerViewModel.removeLastContainer()
        val currentContainer = sharedContainerViewModel.currentContainer()
        when (currentContainer) {
            is SignedContainer -> {
                sharedContainerViewModel.resetCryptoContainer()
                sharedContainerViewModel.setSignedContainer(currentContainer)
            }
            is CryptoContainer -> {
                sharedContainerViewModel.resetSignedContainer()
                sharedContainerViewModel.setCryptoContainer(currentContainer)
                navController.navigateUp()
            }
        }
    } else {
        sharedContainerViewModel.clearContainers()
        signingViewModel.handleBackButton()
        navController.navigateUp()
    }
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
                    ).setType(mimetype)
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
fun SigningNavigationPreview() {
    RIADigiDocTheme {
        SigningNavigation(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedContainerViewModel = hiltViewModel(),
            sharedSignatureViewModel = hiltViewModel(),
        )
    }
}
