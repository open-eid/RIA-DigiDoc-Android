@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.ui.component.settings.EditValueDialog
import ee.ria.DigiDoc.ui.component.shared.ContainerMessage
import ee.ria.DigiDoc.ui.component.shared.DataFileItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.shared.dialog.SivaConfirmationDialog
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.ContainerBottomSheet
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.DataFileBottomSheet
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.SignatureBottomSheet
import ee.ria.DigiDoc.ui.component.signing.bottomsheet.SignedContainerBottomSheet
import ee.ria.DigiDoc.ui.component.toast.ToastUtil.showMessage
import ee.ria.DigiDoc.ui.theme.Dimensions.MAX_DIALOG_WIDTH
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Green500
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.removeExtensionFromContainerFilename
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel
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
fun SigningNavigation(
    activity: Activity,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSignatureViewModel: SharedSignatureViewModel,
    signingViewModel: SigningViewModel = hiltViewModel(),
) {
    val signatureAddController = rememberNavController()
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val shouldResetContainer by signingViewModel.shouldResetSignedContainer.asFlow().collectAsState(false)
    val context = LocalContext.current

    val clickedDataFile = remember { mutableStateOf<DataFileInterface?>(null) }
    val clickedSignature = remember { mutableStateOf<SignatureInterface?>(null) }

    val signatureAddedSuccess = remember { mutableStateOf(false) }
    val signatureAddedSuccessText = stringResource(id = R.string.signature_update_signature_add_success)

    val xadesText = stringResource(id = R.string.xades_file_message)
    val cadesText = stringResource(id = R.string.cades_file_message)

    val containerTitle = stringResource(id = R.string.container_title)
    val emptyFileInContainerText = stringResource(id = R.string.empty_file_message)

    val isNestedContainer = sharedContainerViewModel.isNestedContainer(signedContainer)
    val isXadesContainer = signedContainer?.isXades() == true
    val isCadesContainer = signedContainer?.isCades() == true

    var validSignaturesCount by remember { mutableIntStateOf(0) }
    var unknownSignaturesCount by remember { mutableIntStateOf(0) }
    var invalidSignaturesCount by remember { mutableIntStateOf(0) }

    var isSignaturesCountLoaded by remember { mutableStateOf(false) }

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
        AccessibilityUtil.sendAccessibilityEvent(
            context,
            TYPE_ANNOUNCEMENT,
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
        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signingCancelled)
    }

    var signatures by remember { mutableStateOf<List<SignatureInterface>>(emptyList()) }
    val showSignaturesLoadingIndicator = remember { mutableStateOf(false) }
    val signaturesLoading = stringResource(id = R.string.signatures_loading)
    val signaturesLoaded = stringResource(id = R.string.signatures_loaded)

    var dataFiles by remember { mutableStateOf<List<DataFileInterface>>(emptyList()) }
    val showDataFilesLoadingIndicator = remember { mutableStateOf(false) }
    val dataFilesLoading = stringResource(id = R.string.container_files_loading)
    val dataFilesLoaded = stringResource(id = R.string.container_files_loaded)
    val containerFilesLoaded = stringResource(id = R.string.container_files_loaded)

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

    val onSignatureItemClick: (SignatureInterface) -> Unit = { signature ->
        showSignatureBottomSheet.value = true
        clickedSignature.value = signature
    }

    var actionDataFile by remember { mutableStateOf<DataFileInterface?>(null) }

    var isSaved by remember { mutableStateOf<Boolean>(false) }

    val selectedSignedContainerTabIndex = rememberSaveable { mutableIntStateOf(0) }

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    actionDataFile?.let { datafile ->
                        sharedContainerViewModel
                            .getContainerDataFile(signedContainer, datafile)
                            ?.let { sharedContainerViewModel.saveContainerFile(it, result) }
                        showMessage(context, R.string.file_saved)
                        isSaved = true
                    } ?: run {
                        signedContainer?.getContainerFile()?.let {
                            sharedContainerViewModel.saveContainerFile(it, result)
                            showMessage(context, R.string.file_saved)
                            isSaved = true
                        } ?: showMessage(context, R.string.file_saved_error)
                    }
                } catch (e: Exception) {
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
            }
        }
    }

    LaunchedEffect(Unit) {
        sharedContainerViewModel.setSignedContainer(sharedContainerViewModel.currentSignedContainer())
    }

    LaunchedEffect(sharedContainerViewModel.signedMidStatus) {
        sharedContainerViewModel.signedMidStatus.asFlow().collect { status ->
            status?.let {
                if (status == MobileCreateSignatureProcessStatus.OK) {
                    signatures = signedContainer?.getSignatures() ?: emptyList()
                    withContext(Main) {
                        signatureAddedSuccess.value = true
                        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signatureAddedSuccessText)
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
                        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signatureAddedSuccessText)
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
                        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signatureAddedSuccessText)
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
                        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signatureAddedSuccessText)
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
            }
            val newTime = System.currentTimeMillis()
            if (newTime >= (pastTime + 2 * 1000)) {
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signaturesLoaded)
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
                AccessibilityUtil.sendAccessibilityEvent(
                    context,
                    TYPE_ANNOUNCEMENT,
                    announcementText,
                )
            }
        }
    }

    LaunchedEffect(signedContainer) {
        signedContainer?.let {
            val pastTime = System.currentTimeMillis()
            showDataFilesLoadingIndicator.value = true
            dataFiles = it.getDataFiles()
            signingViewModel.showMessage(containerFilesLoaded)
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
                    signingViewModel,
                    sharedContainerViewModel,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        SnackBarManager.messages.collect { message ->
            if (message.isNotEmpty()) {
                snackBarScope.launch {
                    snackBarHostState.showSnackbar(message)
                }
            }
        }
    }

    if (openSignatureDialog.value) {
        BasicAlertDialog(
            onDismissRequest = dismissDialog,
            modifier = modifier.fillMaxWidth(MAX_DIALOG_WIDTH),
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            AddSignatureView(
                activity = activity,
                signatureAddController = signatureAddController,
                cancelButtonClick = cancelButtonClick,
                dismissDialog = dismissDialog,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signingScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title =
                    signedContainer?.takeIf { it.isSigned() }
                        ?.let { R.string.signing_container_documents_title },
                leftIcon = R.drawable.ic_m3_close_48dp_wght400,
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
            )
        },
        bottomBar = {
            SigningBottomBar(
                modifier = modifier,
                onSignClick = {
                    openSignatureDialog.value = true
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
                isUnsignedContainer = signedContainer?.isSigned() == false,
            )
        },
    ) { innerPadding ->
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
            var actionSignature by remember { mutableStateOf<SignatureInterface?>(null) }

            val showSivaDialog = remember { mutableStateOf(false) }
            var nestedFile = rememberSaveable { mutableStateOf<File?>(null) }

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

            val handleSivaCancel: () -> Unit = {
                showSivaDialog.value = false
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

            if (showLoadingScreen.value) {
                LoadingScreen(modifier = modifier)
            }

            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(SPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                if (signatureAddedSuccess.value) {
                    ContainerMessage(
                        modifier = modifier,
                        text = signatureAddedSuccessText,
                        testTag = "signatureAddedSuccess",
                        color = Green500,
                    )
                }

                if (isXadesContainer) {
                    ContainerMessage(
                        modifier = modifier,
                        text = xadesText,
                        testTag = "signatureUpdateListStatusXadesFile",
                    )
                }

                if (isCadesContainer) {
                    ContainerMessage(
                        modifier = modifier,
                        text = cadesText,
                        testTag = "signatureUpdateListStatusCadesFile",
                    )
                }

                if (unknownSignaturesCount > 0) {
                    ContainerMessage(
                        modifier = modifier,
                        text = unknownSignaturesText,
                        testTag = "signatureUpdateListStatusUnknown",
                    )
                }

                if (invalidSignaturesCount > 0) {
                    ContainerMessage(
                        modifier = modifier,
                        text = invalidSignaturesText,
                        testTag = "signatureUpdateListStatusInvalid",
                    )
                }

                if (signingViewModel.isEmptyFileInContainer(signedContainer)) {
                    ContainerMessage(
                        modifier = modifier,
                        text = emptyFileInContainerText,
                        testTag = "signatureUpdateListStatusEmptyFile",
                    )
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
                                    modifier = modifier.padding(bottom = SPadding),
                                    text = stringResource(R.string.signature_update_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Start,
                                )
                            }
                            ContainerNameView(
                                icon = R.drawable.ic_m3_stylus_note_48dp_wght400,
                                name = signedContainerName,
                                showActionButtons = isSignedContainer,
                                leftActionButtonName = R.string.signature_update_signature_add,
                                rightActionButtonName = R.string.crypto_button,
                                leftActionButtonContentDescription = R.string.signature_update_signature_add,
                                rightActionButtonContentDescription = R.string.crypto_button_accessibility,
                                onLeftActionButtonClick = {
                                    openSignatureDialog.value = true
                                },
                                onRightActionButtonClick = {
                                    // TODO: Implement encrypt click
                                },
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
                                            .padding(vertical = screenViewExtraLargePadding),
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
                            if (signingViewModel.isContainerWithoutSignatures(signedContainer) && !isNestedContainer) {
                                item {
                                    Text(
                                        modifier =
                                            modifier
                                                .padding(horizontal = SPadding)
                                                .padding(top = SPadding),
                                        text = stringResource(R.string.signing_documents_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Start,
                                    )
                                    DataFileItem(modifier, dataFiles, onDataFileClick)
                                }
                            } else {
                                item {
                                    TabView(
                                        modifier = modifier.padding(top = MPadding),
                                        selectedTabIndex = selectedSignedContainerTabIndex.intValue,
                                        onTabSelected = { index -> selectedSignedContainerTabIndex.intValue = index },
                                        listOf(
                                            Pair(
                                                stringResource(R.string.signing_documents_title),
                                                { DataFileItem(modifier, dataFiles, onDataFileClick) },
                                            ),
                                            Pair(
                                                stringResource(R.string.signing_container_signatures_title),
                                                {
                                                    SignatureComponent(
                                                        modifier,
                                                        signatures,
                                                        showSignaturesLoadingIndicator.value,
                                                        signaturesLoading,
                                                        onSignatureItemClick,
                                                    )
                                                },
                                            ),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    // TODO: Add timestamp after design update
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
                                .padding(itemSpacingPadding)
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
                                CoroutineScope(IO).launch {
                                    signedContainer?.setName(
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
                                if ((signedContainer?.rawContainer()?.dataFiles()?.size ?: 0) == 1) {
                                    signedContainer?.getContainerFile()?.delete()
                                    sharedContainerViewModel.resetSignedContainer()
                                    handleBackButtonClick(navController, signingViewModel, sharedContainerViewModel)
                                } else {
                                    CoroutineScope(IO).launch {
                                        try {
                                            sharedContainerViewModel.removeContainerDataFile(
                                                signedContainer,
                                                actionDataFile,
                                            )
                                        } catch (e: Exception) {
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
                                .padding(vertical = screenViewLargePadding)
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
                                CoroutineScope(IO).launch {
                                    sharedContainerViewModel.removeSignature(
                                        signedContainer,
                                        actionSignature,
                                    )
                                }
                                closeSignatureDialog()
                                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signatureRemoved)
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
                    clickedDataFile.value = null
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
                openSignatureDialog = openSignatureDialog,
                onEncryptClick = {
                    // TODO: Implement encrypt click
                },
                onExtendSignatureClick = {
                    // TODO: Implement extend signature click
                },
            )

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
                        signedContainer?.getContainerFile()?.delete()
                        sharedContainerViewModel.resetSignedContainer()
                        handleBackButtonClick(navController, signingViewModel, sharedContainerViewModel)
                    },
                )
            }
        }
    }
}

fun handleBackButtonClick(
    navController: NavHostController,
    signingViewModel: SigningViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    sharedContainerViewModel.resetExternalFileUris()
    if (sharedContainerViewModel.nestedContainers.size > 1) {
        sharedContainerViewModel.removeLastContainer()
        sharedContainerViewModel.setSignedContainer(sharedContainerViewModel.currentSignedContainer())
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
                    )
                    .setType(mimetype)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                null,
            )
        saveFileLauncher.launch(saveIntent)
    } catch (e: ActivityNotFoundException) {
        // No activity to handle this kind of files
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
fun SigningNavigationPreview() {
    val navController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val sharedSignatureViewModel: SharedSignatureViewModel = hiltViewModel()

    RIADigiDocTheme {
        SigningNavigation(
            activity = LocalActivity.current as Activity,
            navController = navController,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedSignatureViewModel = sharedSignatureViewModel,
        )
    }
}
