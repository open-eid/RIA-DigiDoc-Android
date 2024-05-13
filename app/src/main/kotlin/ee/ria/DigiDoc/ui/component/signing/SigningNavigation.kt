@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getDataFileMimetype
import ee.ria.DigiDoc.ui.component.ContainerFile
import ee.ria.DigiDoc.ui.component.ContainerName
import ee.ria.DigiDoc.ui.component.settings.EditValueDialog
import ee.ria.DigiDoc.ui.component.settings.MessageDialog
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.removeExtensionFromContainerFilename
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil.showMessage
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.SigningViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigningNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedContainerViewModel: SharedContainerViewModel,
    signingViewModel: SigningViewModel = hiltViewModel(),
) {
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val shouldResetContainer by signingViewModel.shouldResetSignedContainer
    val context = LocalContext.current
    BackHandler {
        handleBackButtonClick(navController, signingViewModel)
    }

    DisposableEffect(shouldResetContainer) {
        onDispose {
            if (shouldResetContainer) {
                sharedContainerViewModel.resetSignedContainer()
            }
        }
    }

    Scaffold(
        bottomBar = {
            SigningBottomBar(modifier = modifier)
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
            val dismissRemoveFileDialog = {
                openRemoveFileDialog.value = false
            }
            val openEditContainerNameDialog = remember { mutableStateOf(false) }
            val dismissEditContainerNameDialog = {
                openEditContainerNameDialog.value = false
            }
            var signedContainerName = signedContainer?.getName() ?: ""
            var containerName by remember { mutableStateOf(TextFieldValue(text = signedContainerName)) }
            Column {
                // Added top bar here instead of Scaffold -> topBar
                // To better support keyboard navigation
                SigningTopBar(
                    navController,
                    modifier = modifier,
                    onBackButtonClick = {
                        handleBackButtonClick(navController, signingViewModel)
                    },
                )
                Column(
                    modifier =
                        modifier
                            .padding(
                                horizontal = screenViewHorizontalPadding,
                                vertical = screenViewVerticalPadding,
                            )
                            .verticalScroll(rememberScrollState()),
                ) {
                    signedContainerName = signedContainer?.getName() ?: ""
                    containerName = TextFieldValue(text = removeExtensionFromContainerFilename(signedContainerName))
                    ContainerName(
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
                                    horizontal = screenViewHorizontalPadding,
                                    vertical = screenViewVerticalPadding,
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
                        if (signingViewModel.isExistingContainerNoSignatures(signedContainer)) {
                            IconButton(
                                onClick = {
                                    navController.navigate(
                                        Route.FileChoosing.route,
                                    )
                                },
                                modifier = modifier.size(iconSize),
                                content = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_add_circle),
                                        contentDescription =
                                            stringResource(
                                                id = R.string.documents_add_button_accessibility,
                                            ).lowercase(),
                                        tint = Blue500,
                                    )
                                },
                            )
                        }
                    }
                    signedContainer?.getDataFiles()?.forEach { dataFile ->
                        val file = sharedContainerViewModel.getContainerDataFile(signedContainer, dataFile)
                        ContainerFile(
                            dataFile = dataFile,
                            onClickView = {
                                try {
                                    val uri =
                                        FileProvider.getUriForFile(
                                            context,
                                            context.getString(R.string.file_provider_authority),
                                            file!!,
                                        )
                                    val shareIntent = Intent()
                                    shareIntent.setAction(Intent.ACTION_VIEW)
                                    shareIntent.setDataAndType(
                                        uri,
                                        file.let { SignedContainer.mimeType(it) },
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

                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = screenViewHorizontalPadding,
                                    vertical = screenViewVerticalPadding,
                                ),
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
                        )
                    }

                    if (signingViewModel.isExistingContainer(signedContainer)) {
                        HorizontalDivider(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenViewHorizontalPadding)
                                    .height(dividerHeight),
                        )

                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = screenViewHorizontalPadding,
                                        vertical = screenViewVerticalPadding,
                                    ),
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
                        if (signingViewModel.isContainerWithoutSignatures(signedContainer)) {
                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = screenViewHorizontalPadding,
                                            vertical = screenViewVerticalPadding,
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

                        signedContainer?.getSignatures()?.forEach { signature ->
                            SignatureComponent(signature = signature, signingViewModel = signingViewModel)
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
                                .padding(Dimensions.alertDialogOuterPadding),
                    ) {
                        EditValueDialog(
                            title = stringResource(id = R.string.signature_update_name_update_name),
                            editValue = containerName,
                            onEditValueChange = {
                                containerName = it
                            },
                            cancelButtonClick = dismissEditContainerNameDialog,
                            okButtonClick = {
                                signedContainer?.setName(containerName.text)
                                openEditContainerNameDialog.value = false
                            },
                        )
                    }
                }
            }
            var removeFileDialogTitle =
                stringResource(id = R.string.signature_update_remove_document_confirmation_message)
            if ((signedContainer?.getDataFiles()?.size ?: 0) == 1) {
                removeFileDialogTitle =
                    stringResource(id = R.string.signature_update_remove_last_document_confirmation_message)
            }
            if (openRemoveFileDialog.value) {
                BasicAlertDialog(
                    onDismissRequest = dismissRemoveFileDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(Dimensions.alertDialogOuterPadding),
                    ) {
                        MessageDialog(
                            title = removeFileDialogTitle,
                            cancelButtonClick = dismissRemoveFileDialog,
                            okButtonClick = {
                                if ((signedContainer?.getDataFiles()?.size ?: 0) == 1) {
                                    signedContainer?.getContainerFile()?.delete()
                                    sharedContainerViewModel.resetSignedContainer()
                                } else {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        sharedContainerViewModel.setSignedContainer(
                                            sharedContainerViewModel.removeContainerDataFile(
                                                signedContainer,
                                                actionDataFile,
                                            ),
                                        )
                                    }
                                }
                                openRemoveFileDialog.value = false
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
    navController.popBackStack(navController.graph.findStartDestination().id, false)
    signingViewModel.handleBackButton()
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SigningNavigationPreview() {
    val navController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        SigningNavigation(navController, sharedContainerViewModel = sharedContainerViewModel)
    }
}
