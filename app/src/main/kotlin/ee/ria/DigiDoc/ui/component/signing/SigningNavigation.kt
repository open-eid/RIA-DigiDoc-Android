@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getDataFileMimetype
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.ui.component.ContainerFile
import ee.ria.DigiDoc.ui.component.ContainerName
import ee.ria.DigiDoc.ui.component.settings.EditValueDialog
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Dimensions.MAX_DIALOG_WIDTH
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Green500
import ee.ria.DigiDoc.ui.theme.Normal
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.removeExtensionFromContainerFilename
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil.showMessage
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigningNavigation(
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
    BackHandler {
        handleBackButtonClick(navController, signingViewModel)
    }

    DisposableEffect(shouldResetContainer) {
        onDispose {
            if (shouldResetContainer == true) {
                sharedContainerViewModel.resetSignedContainer()
            }
        }
    }

    LaunchedEffect(sharedContainerViewModel.signedMidStatus) {
        sharedContainerViewModel.signedMidStatus.asFlow().collect { status ->
            status?.let {
                if (status == MobileCreateSignatureProcessStatus.OK) {
                    withContext(Dispatchers.Main) {
                        signatureAddedSuccess.value = true
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
                    withContext(Dispatchers.Main) {
                        signatureAddedSuccess.value = true
                        delay(5000)
                        signatureAddedSuccess.value = false
                        sharedContainerViewModel.setSignedSidStatus(null)
                    }
                }
            }
        }
    }

    val openSignatureDialog = remember { mutableStateOf(false) }
    val signingCancelled = stringResource(id = R.string.signing_cancelled)
    val dismissDialog = {
        openSignatureDialog.value = false
        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signingCancelled)
    }

    if (openSignatureDialog.value) {
        BasicAlertDialog(
            onDismissRequest = dismissDialog,
            modifier = modifier.fillMaxWidth(MAX_DIALOG_WIDTH),
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            AddSignatureView(
                signatureAddController = signatureAddController,
                dismissDialog = dismissDialog,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
    }

    Scaffold(
        bottomBar = {
            SigningBottomBar(
                modifier = modifier,
                showSignButton = signingViewModel.isSignButtonShown(signedContainer),
                showEncryptButton = signingViewModel.isEncryptButtonShown(signedContainer),
                showShareButton = signingViewModel.isShareButtonShown(signedContainer),
                onSignClick = {
                    openSignatureDialog.value = true
                },
                onEncryptClick = {
                    // TODO: Implement encrypt click
                },
                onShareClick = {
                    // TODO: Implement share click
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusGroup(),
        ) {
            var actionDataFile by remember { mutableStateOf<DataFileInterface?>(null) }
            var actionSignature by remember { mutableStateOf<SignatureInterface?>(null) }
            val saveFileLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        actionDataFile?.let { datafile ->
                            sharedContainerViewModel.getContainerDataFile(signedContainer, datafile)
                                ?.let { sharedContainerViewModel.saveContainerFile(it, result) }
                        }
                        showMessage(context, R.string.file_saved)
                    }
                }
            val openRemoveFileDialog = remember { mutableStateOf(false) }
            val fileRemoved = stringResource(id = R.string.document_removed)
            val fileRemovalCancelled = stringResource(id = R.string.document_removal_cancelled)
            val closeRemoveFileDialog = {
                openRemoveFileDialog.value = false
            }
            val dismissRemoveFileDialog = {
                closeRemoveFileDialog()
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, fileRemovalCancelled)
            }
            val openRemoveSignatureDialog = remember { mutableStateOf(false) }
            val signatureRemoved = stringResource(id = R.string.signature_removed)
            val signatureRemovalCancelled = stringResource(id = R.string.signature_removal_cancelled)
            val closeSignatureDialog = {
                openRemoveSignatureDialog.value = false
            }
            val dismissRemoveSignatureDialog = {
                closeSignatureDialog()
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, signatureRemovalCancelled)
            }

            val containerNameChanged = stringResource(id = R.string.container_name_changed)
            val containerNameChangeCancelled =
                stringResource(
                    id = R.string.container_name_change_cancelled,
                )

            val openEditContainerNameDialog = remember { mutableStateOf(false) }
            val dismissEditContainerNameDialog = {
                openEditContainerNameDialog.value = false
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, containerNameChangeCancelled)
            }
            var signedContainerName = signedContainer?.getName() ?: ""
            var containerName by remember { mutableStateOf(TextFieldValue(text = signedContainerName)) }
            val containerExtension = FilenameUtils.getExtension(signedContainerName)

            Column {
                // Added top bar here instead of Scaffold -> topBar
                // To better support keyboard navigation
                TopBar(
                    modifier = modifier,
                    title = R.string.signing_title_container_existing,
                    onBackButtonClick = {
                        handleBackButtonClick(navController, signingViewModel)
                    },
                )
                if (signatureAddedSuccess.value) {
                    Column(
                        modifier =
                            modifier
                                .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Surface(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            color = Green500,
                        ) {
                            Text(
                                modifier = modifier.padding(vertical = itemSpacingPadding),
                                text = stringResource(id = R.string.signature_update_signature_add_success),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.background,
                                fontWeight = FontWeight.Normal,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }

                Column(
                    modifier =
                        modifier
                            .verticalScroll(rememberScrollState()),
                ) {
                    signedContainerName = signedContainer?.getName() ?: ""
                    containerName = TextFieldValue(text = removeExtensionFromContainerFilename(signedContainerName))
                    ContainerName(
                        modifier =
                            modifier
                                .background(color = Normal),
                        name = signedContainerName,
                        isContainerSigned = signedContainer?.getSignatures()?.isNotEmpty() == true,
                        onEditNameClick = {
                            openEditContainerNameDialog.value = true
                        },
                        onSaveContainerClick = {
                            // TODO: Implement save container
                        },
                    )

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
                                    .semantics { heading() },
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

                    signedContainer?.getDataFiles()?.forEach { dataFile ->
                        val file = sharedContainerViewModel.getContainerDataFile(signedContainer, dataFile)
                        ContainerFile(
                            dataFile = dataFile,
                            showRemoveButton = signingViewModel.isContainerWithoutSignatures(signedContainer),
                            onClickView = {
                                try {
                                    val uri =
                                        file?.let {
                                            FileProvider.getUriForFile(
                                                context,
                                                context.getString(R.string.file_provider_authority),
                                                it,
                                            )
                                        }
                                    val shareIntent = Intent()
                                    shareIntent.setAction(Intent.ACTION_VIEW)
                                    shareIntent.setDataAndType(
                                        uri,
                                        file?.let { SignedContainer.mimeType(it) },
                                    )
                                    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    ContextCompat.startActivity(context, shareIntent, null)
                                } catch (e: ActivityNotFoundException) {
                                    // no Activity to handle this kind of files
                                }
                            },
                            onClickRemove = {
                                actionDataFile = dataFile
                                openRemoveFileDialog.value = true
                            },
                            onClickSave = {
                                try {
                                    actionDataFile = dataFile
                                    val saveIntent =
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_CREATE_DOCUMENT)
                                                .addCategory(Intent.CATEGORY_OPENABLE)
                                                .putExtra(
                                                    Intent.EXTRA_TITLE,
                                                    sanitizeString(dataFile.fileName, ""),
                                                )
                                                .setType(getDataFileMimetype(dataFile))
                                                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                                            null,
                                        )
                                    saveFileLauncher.launch(saveIntent)
                                } catch (e: ActivityNotFoundException) {
                                    // no Activity to handle this kind of files
                                }
                            },
                        )
                    }

                    if (signingViewModel.isContainerWithoutSignatures(signedContainer)) {
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(screenViewLargePadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PrimaryButton(
                                onClickItem = {
                                    navController.navigate(
                                        Route.FileChoosing.route,
                                    )
                                },
                                title = R.string.documents_add_button,
                                contentDescription = stringResource(id = R.string.documents_add_button_accessibility),
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                isSubButton = true,
                            )
                        }
                    }

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
                                    .semantics { heading() },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    if (signingViewModel.isExistingContainer(signedContainer) ||
                        !signingViewModel.isContainerWithoutSignatures(signedContainer)
                    ) {
                        signedContainer?.getSignatures()?.forEach { signature ->
                            HorizontalDivider(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(screenViewLargePadding)
                                        .height(dividerHeight),
                            )

                            SignatureComponent(
                                signature = signature,
                                signingViewModel = signingViewModel,
                                showRemoveButton =
                                    !NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS.contains(
                                        FilenameUtils.getExtension(signedContainer?.getName()),
                                    ),
                                onRemoveButtonClick = {
                                    actionSignature = signature
                                    openRemoveSignatureDialog.value = true
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

                    if (signingViewModel.isContainerWithoutSignatures(signedContainer)) {
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
                                modifier = modifier.weight(1f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            if (openEditContainerNameDialog.value) {
                BasicAlertDialog(
                    onDismissRequest = dismissEditContainerNameDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(itemSpacingPadding),
                    ) {
                        EditValueDialog(
                            title = stringResource(id = R.string.signature_update_name_update_name),
                            editValue = containerName,
                            onEditValueChange = {
                                containerName = it
                            },
                            cancelButtonClick = dismissEditContainerNameDialog,
                            okButtonClick = {
                                signedContainer?.setName("${containerName.text}.$containerExtension")
                                openEditContainerNameDialog.value = false
                                AccessibilityUtil.sendAccessibilityEvent(
                                    context,
                                    TYPE_ANNOUNCEMENT,
                                    containerNameChanged,
                                )
                            },
                        )
                    }
                }
            }
            var removeFileDialogTitle =
                stringResource(id = R.string.document_remove_confirmation_message)
            if ((signedContainer?.getDataFiles()?.size ?: 0) == 1) {
                removeFileDialogTitle =
                    stringResource(id = R.string.document_remove_last_confirmation_message)
            }
            val removeSignatureDialogTitle =
                stringResource(id = R.string.signature_update_signature_remove_confirmation_message)
            if (openRemoveFileDialog.value) {
                BasicAlertDialog(
                    onDismissRequest = dismissRemoveFileDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .padding(screenViewLargePadding)
                                .verticalScroll(rememberScrollState()),
                    ) {
                        MessageDialog(
                            title = removeFileDialogTitle,
                            cancelButtonClick = dismissRemoveFileDialog,
                            okButtonClick = {
                                if ((signedContainer?.getDataFiles()?.size ?: 0) == 1) {
                                    signedContainer?.getContainerFile()?.delete()
                                    sharedContainerViewModel.resetSignedContainer()
                                    handleBackButtonClick(navController, signingViewModel)
                                } else {
                                    CoroutineScope(IO).launch {
                                        sharedContainerViewModel.removeContainerDataFile(
                                            signedContainer,
                                            actionDataFile,
                                        )
                                    }
                                }
                                closeRemoveFileDialog()
                                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, fileRemoved)
                            },
                        )
                    }
                }
            }
            if (openRemoveSignatureDialog.value) {
                BasicAlertDialog(
                    onDismissRequest = dismissRemoveSignatureDialog,
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
                        )
                    }
                }
            }
        }
    }
}

fun handleBackButtonClick(
    navController: NavHostController,
    signingViewModel: SigningViewModel,
) {
    signingViewModel.handleBackButton()
    navController.navigateUp()
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
            navController = navController,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedSignatureViewModel = sharedSignatureViewModel,
        )
    }
}
