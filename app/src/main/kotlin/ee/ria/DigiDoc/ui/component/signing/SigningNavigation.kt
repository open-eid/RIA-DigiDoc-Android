@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
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
import ee.ria.DigiDoc.common.Constant.NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.NO_REMOVE_SIGNATURE_BUTTON_FILE_MIMETYPES
import ee.ria.DigiDoc.common.Constant.SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.ui.component.ContainerFile
import ee.ria.DigiDoc.ui.component.ContainerName
import ee.ria.DigiDoc.ui.component.settings.EditValueDialog
import ee.ria.DigiDoc.ui.component.shared.ContainerMessage
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.shared.dialog.SivaConfirmationDialog
import ee.ria.DigiDoc.ui.component.toast.ToastUtil.showMessage
import ee.ria.DigiDoc.ui.theme.Dimensions.MAX_DIALOG_WIDTH
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Green500
import ee.ria.DigiDoc.ui.theme.Normal
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.removeExtensionFromContainerFilename
import ee.ria.DigiDoc.utilsLib.extensions.isContainer
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

    val signatureAddedSuccess = remember { mutableStateOf(false) }
    val signatureAddedSuccessText = stringResource(id = R.string.signature_update_signature_add_success)

    val xadesText = stringResource(id = R.string.xades_file_message)
    val cadesText = stringResource(id = R.string.cades_file_message)

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
    var removeFileDialogTitle =
        stringResource(id = R.string.document_remove_confirmation_message)
    val removeFileCancelButtonContentDescription =
        stringResource(id = R.string.document_cancel_removal_button)
    val removeFileOkButtonContentDescription =
        stringResource(id = R.string.document_confirm_removal_button)
    if ((signedContainer?.rawContainer()?.dataFiles()?.size ?: 0) == 1) {
        removeFileDialogTitle =
            stringResource(id = R.string.document_remove_last_confirmation_message)
    }
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
    val removeSignatureDialogTitle =
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

    val listState = rememberLazyListState()

    BackHandler {
        handleBackButtonClick(navController, signingViewModel, sharedContainerViewModel)
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
            showDataFilesLoadingIndicator.value = false
            val newTime = System.currentTimeMillis()
            if (newTime >= (pastTime + 2 * 1000)) {
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, dataFilesLoaded)
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
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signingScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.signing_title_container_existing,
                onBackButtonClick = {
                    handleBackButtonClick(
                        navController,
                        signingViewModel,
                        sharedContainerViewModel,
                    )
                },
            )
        },
        bottomBar = {
            SigningBottomBar(
                modifier = modifier,
                showSignButton =
                    signingViewModel.isSignButtonShown(
                        signedContainer,
                        isNestedContainer,
                        isXadesContainer,
                        isCadesContainer,
                    ),
                showEncryptButton = signingViewModel.isEncryptButtonShown(signedContainer, isNestedContainer),
                showShareButton = signingViewModel.isShareButtonShown(signedContainer, isNestedContainer),
                onSignClick = {
                    openSignatureDialog.value = true
                },
                onEncryptClick = {
                    // TODO: Implement encrypt click
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
            )
        },
    ) { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusGroup()
                    .semantics {
                        testTagsAsResourceId = true
                    },
        ) {
            var actionDataFile by remember { mutableStateOf<DataFileInterface?>(null) }
            var actionSignature by remember { mutableStateOf<SignatureInterface?>(null) }
            val saveFileLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        try {
                            actionDataFile?.let { datafile ->
                                sharedContainerViewModel
                                    .getContainerDataFile(signedContainer, datafile)
                                    ?.let { sharedContainerViewModel.saveContainerFile(it, result) }
                                showMessage(context, R.string.file_saved)
                            } ?: run {
                                signedContainer?.getContainerFile()?.let {
                                    sharedContainerViewModel.saveContainerFile(it, result)
                                    showMessage(context, R.string.file_saved)
                                } ?: showMessage(context, R.string.file_saved_error)
                            }
                        } catch (e: Exception) {
                            showMessage(context, R.string.file_saved_error)
                        }
                    }
                }

            val showSivaDialog = remember { mutableStateOf(false) }
            var nestedFile by remember { mutableStateOf<File?>(null) }

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
                nestedFile?.let { file ->
                    openNestedContainer(file, true)
                }
            }

            val handleSivaCancel: () -> Unit = {
                showSivaDialog.value = false
                nestedFile?.let { file ->
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
                modifier = modifier,
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
                            ContainerName(
                                modifier =
                                    modifier
                                        .background(color = Normal),
                                name = signedContainerName,
                                isContainerSigned = it.isSigned(),
                                isNestedContainer = isNestedContainer,
                                onEditNameClick = {
                                    openEditContainerNameDialog.value = true
                                },
                                onSaveContainerClick = {
                                    actionDataFile = null
                                    val containerFile = signedContainer?.getContainerFile()
                                    if (containerFile != null) {
                                        saveFile(
                                            containerFile,
                                            signingViewModel.getMimetype(containerFile),
                                            saveFileLauncher,
                                        )
                                    }
                                },
                            )
                        }
                    }
                    item {
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = screenViewLargePadding,
                                        vertical = screenViewExtraLargePadding,
                                    ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val containerDocumentsTitle =
                                if (signingViewModel.isExistingContainerNoSignatures(signedContainer)) {
                                    R.string.signing_container_documents_title
                                } else {
                                    R.string.signing_documents_title
                                }
                            Text(
                                stringResource(
                                    id = containerDocumentsTitle,
                                ),
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics { heading() }
                                        .testTag("signatureUpdateListDocumentsSubheadTitle"),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }

                        HorizontalDivider(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = screenViewLargePadding,
                                        vertical = itemSpacingPadding,
                                    )
                                    .height(dividerHeight),
                        )
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
                            items(dataFiles) { dataFile ->
                                ContainerFile(
                                    modifier = modifier,
                                    dataFile = dataFile,
                                    showRemoveButton =
                                        signingViewModel.isContainerWithoutSignatures(
                                            signedContainer,
                                        ),
                                    onClickView = {
                                        try {
                                            val containerDataFile =
                                                sharedContainerViewModel.getContainerDataFile(
                                                    signedContainer,
                                                    dataFile,
                                                )
                                            showLoadingScreen.value = false
                                            containerDataFile?.let { dataFile ->
                                                if (dataFile.isContainer(context)) {
                                                    nestedFile = dataFile
                                                    val nestedFileMimetype = dataFile.mimeType(context)
                                                    if (SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(
                                                            nestedFileMimetype,
                                                        )
                                                    ) {
                                                        showSivaDialog.value = true
                                                    } else {
                                                        showLoadingScreen.value = true
                                                        handleSivaConfirmation()
                                                    }
                                                } else {
                                                    val viewIntent =
                                                        signingViewModel.getViewIntent(
                                                            context,
                                                            dataFile,
                                                        )
                                                    context.startActivity(
                                                        viewIntent,
                                                        null,
                                                    )
                                                }
                                            }
                                        } catch (ex: Exception) {
                                            errorLog(
                                                this.javaClass.simpleName,
                                                "Unable to open container. Unable to get datafiles",
                                                ex,
                                            )
                                            handleBackButtonClick(
                                                navController,
                                                signingViewModel,
                                                sharedContainerViewModel,
                                            )
                                        }
                                    },
                                    onClickRemove = {
                                        actionDataFile = dataFile
                                        openRemoveFileDialog.value = true
                                    },
                                    onClickSave = {
                                        try {
                                            val file =
                                                sharedContainerViewModel.getContainerDataFile(
                                                    signedContainer,
                                                    dataFile,
                                                )
                                            actionDataFile = dataFile
                                            saveFile(file, dataFile.mediaType, saveFileLauncher)
                                        } catch (ex: Exception) {
                                            errorLog(
                                                this.javaClass.simpleName,
                                                "Unable to save file. Unable to get datafiles",
                                                ex,
                                            )
                                            handleBackButtonClick(
                                                navController,
                                                signingViewModel,
                                                sharedContainerViewModel,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }

                    if (signingViewModel.isContainerWithoutSignatures(signedContainer) && !isNestedContainer) {
                        item {
                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(screenViewLargePadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PrimaryButton(
                                    modifier =
                                        modifier
                                            .testTag("signatureUpdateListDocumentsSubheadButton"),
                                    onClickItem = {
                                        navController.navigate(
                                            Route.FileChoosing.route,
                                        )
                                    },
                                    title = R.string.documents_add_button,
                                    contentDescription =
                                        stringResource(
                                            id = R.string.documents_add_button_accessibility,
                                        ),
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    isSubButton = true,
                                )
                            }
                        }
                    }

                    if (signingViewModel.isContainerWithTimestamps(signedContainer)) {
                        item {
                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(screenViewLargePadding),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(
                                        id = R.string.signing_container_timestamps_title,
                                    ),
                                    modifier =
                                        modifier
                                            .weight(1f)
                                            .semantics { heading() }
                                            .testTag("signatureUpdateListTimestampsSubheadTitle"),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                        }

                        item {
                            HorizontalDivider(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(screenViewLargePadding)
                                        .height(dividerHeight)
                                        .notAccessible(),
                            )
                        }

                        signedContainer?.let { container ->
                            container.getTimestamps()?.let { timestamps ->
                                itemsIndexed(timestamps) { index, signature ->
                                    SignatureComponent(
                                        testTag = "signatureUpdateListTimestamps$index",
                                        modifier = modifier,
                                        signature = signature,
                                        signingViewModel = signingViewModel,
                                        showRemoveButton =
                                            !NO_REMOVE_SIGNATURE_BUTTON_FILE_MIMETYPES.contains(
                                                signedContainer?.getContainerFile()
                                                    ?.let { signingViewModel.getMimetype(it) },
                                            ) &&
                                                !NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS.contains(
                                                    FilenameUtils.getExtension(signedContainer?.getName()),
                                                ) && !isNestedContainer && !isXadesContainer && !isCadesContainer,
                                        onRemoveButtonClick = {
                                            actionSignature = signature
                                            openRemoveSignatureDialog.value = true
                                        },
                                        showRolesDetailsButton =
                                            !signingViewModel.isRoleEmpty(
                                                signature = signature,
                                            ),
                                        onRolesDetailsButtonClick = {
                                            sharedSignatureViewModel.setSignature(signature)
                                            navController.navigate(
                                                Route.RolesDetail.route,
                                            )
                                        },
                                        onSignerDetailsButtonClick = {
                                            sharedSignatureViewModel.setSignature(signature)
                                            navController.navigate(
                                                Route.SignerDetail.route,
                                            )
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
                            }
                        }
                    }

                    item {
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(screenViewLargePadding),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(
                                    id = R.string.signing_container_signatures_title,
                                ),
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics { heading() }
                                        .testTag("signatureUpdateListSignaturesSubheadTitle"),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }

                    if (signingViewModel.isExistingContainer(signedContainer) ||
                        !signingViewModel.isContainerWithoutSignatures(signedContainer)
                    ) {
                        if (!signingViewModel.isExistingContainerNoSignatures(signedContainer)) {
                            item {
                                HorizontalDivider(
                                    modifier =
                                        modifier
                                            .fillMaxWidth()
                                            .padding(screenViewLargePadding)
                                            .height(dividerHeight),
                                )
                            }
                        }
                        signedContainer?.let {
                            if (showSignaturesLoadingIndicator.value) {
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
                                                        this.contentDescription = signaturesLoading
                                                    }
                                                    .testTag("signaturesLoadingProgress"),
                                        )
                                    }
                                }
                            } else {
                                itemsIndexed(signatures) { index, signature ->
                                    SignatureComponent(
                                        testTag = "signatureUpdateListSignature$index",
                                        modifier = modifier,
                                        signature = signature,
                                        signingViewModel = signingViewModel,
                                        showRemoveButton =
                                            !NO_REMOVE_SIGNATURE_BUTTON_FILE_MIMETYPES.contains(
                                                signedContainer?.getContainerFile()
                                                    ?.let { container -> signingViewModel.getMimetype(container) },
                                            ) &&
                                                !NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS.contains(
                                                    FilenameUtils.getExtension(signedContainer?.getName()),
                                                ) && !isNestedContainer && !isXadesContainer && !isCadesContainer,
                                        onRemoveButtonClick = {
                                            actionSignature = signature
                                            openRemoveSignatureDialog.value = true
                                        },
                                        showRolesDetailsButton =
                                            !signingViewModel.isRoleEmpty(
                                                signature = signature,
                                            ),
                                        onRolesDetailsButtonClick = {
                                            sharedSignatureViewModel.setSignature(signature)
                                            navController.navigate(
                                                Route.RolesDetail.route,
                                            )
                                        },
                                        onSignerDetailsButtonClick = {
                                            sharedSignatureViewModel.setSignature(signature)
                                            navController.navigate(
                                                Route.SignerDetail.route,
                                            )
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
                            }
                        }
                    }

                    if (signingViewModel.isContainerWithoutSignatures(signedContainer)) {
                        item {
                            HorizontalDivider(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(screenViewLargePadding)
                                        .height(dividerHeight),
                            )

                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = screenViewLargePadding,
                                            vertical = screenViewExtraLargePadding,
                                        ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(
                                        id = R.string.signing_container_signatures_empty,
                                    ),
                                    modifier =
                                        modifier
                                            .weight(1f)
                                            .testTag("signatureUpdateListSignaturesEmpty"),
                                    textAlign = TextAlign.Center,
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
                            editValue = containerName,
                            onEditValueChange = {
                                containerName = it
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
                            modifier =
                                modifier
                                    .testTag("dialogText"),
                            title = removeFileDialogTitle,
                            cancelButtonClick = dismissRemoveFileDialog,
                            okButtonClick = {
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
                            cancelButtonContentDescription = removeFileCancelButtonContentDescription,
                            okButtonContentDescription = removeFileOkButtonContentDescription,
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
                            modifier =
                                modifier
                                    .testTag("dialogText"),
                            title = removeSignatureDialogTitle,
                            cancelButtonClick = dismissRemoveSignatureDialog,
                            okButtonClick = {
                                CoroutineScope(IO).launch {
                                    sharedContainerViewModel.removeSignature(
                                        signedContainer,
                                        actionSignature,
                                    )
                                }
                                closeSignatureDialog()
                                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signatureRemoved)
                            },
                            cancelButtonContentDescription = removeSignatureCancelButtonContentDescription,
                            okButtonContentDescription = removeSignatureOkButtonContentDescription,
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
        }
    }
}

fun handleBackButtonClick(
    navController: NavHostController,
    signingViewModel: SigningViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    sharedContainerViewModel.resetExternalFileUri()
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
            activity = LocalContext.current as Activity,
            navController = navController,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedSignatureViewModel = sharedSignatureViewModel,
        )
    }
}
