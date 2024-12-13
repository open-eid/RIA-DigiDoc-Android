@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.net.Uri
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.dialog.SivaConfirmationDialog
import ee.ria.DigiDoc.ui.component.toast.ToastUtil
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.viewmodel.FileOpeningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FileOpeningNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    fileOpeningViewModel: FileOpeningViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val externalFileUris by sharedContainerViewModel.externalFileUris.collectAsState()
    val showSivaDialog = remember { mutableStateOf(false) }
    var isExternalFile by remember { mutableStateOf(false) }
    var fileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val handleSivaConfirmation: () -> Unit = {
        showSivaDialog.value = false
        fileOpeningViewModel.resetExternalFileState(sharedContainerViewModel)
        CoroutineScope(IO).launch {
            fileOpeningViewModel.handleFiles(context, fileUris, signedContainer, true)
            fileUris = emptyList()
        }
    }

    val handleSivaCancel: () -> Unit = {
        showSivaDialog.value = false
        fileOpeningViewModel.resetExternalFileState(sharedContainerViewModel)
        CoroutineScope(IO).launch {
            val fileMimeType = fileOpeningViewModel.getFileMimetype(fileUris)
            when (fileMimeType) {
                DDOC_MIMETYPE -> {
                    withContext(Main) {
                        navController.popBackStack()
                        fileOpeningViewModel.handleCancelDdocMimeType(context, isExternalFile)
                    }
                }
                ASICS_MIMETYPE -> {
                    fileOpeningViewModel.handleCancelAsicsMimeType(
                        context,
                        fileUris,
                        signedContainer,
                    )
                }
                else -> {
                    withContext(Main) {
                        navController.navigate(Route.Home.route) {
                            popUpTo(Route.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }
            fileUris = emptyList()
            isExternalFile = false
        }
    }

    val handleResult: (Boolean) -> Unit = { confirmed ->
        if (confirmed) {
            handleSivaConfirmation()
        } else {
            handleSivaCancel()
        }
    }

    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
            onResult = { uris ->
                if (uris.isEmpty()) {
                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }
                fileUris = uris

                CoroutineScope(IO).launch {
                    if ((signedContainer?.getDataFiles()?.isNotEmpty() != true) &&
                        fileOpeningViewModel.isSivaConfirmationNeeded(uris)
                    ) {
                        showSivaDialog.value = true
                    } else {
                        handleSivaConfirmation()
                    }
                }
            },
        )

    val fileAddedText = stringResource(id = R.string.file_added)
    val filesAddedText = stringResource(id = R.string.files_added)
    var errorText by remember { mutableStateOf(Pair<Int, String?>(0, null)) }

    LaunchedEffect(fileOpeningViewModel.errorState) {
        fileOpeningViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    if (errorState.first != 0) {
                        errorText = errorState
                    }
                    delay(2000)
                    if (signedContainer == null) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    BackHandler {
        CoroutineScope(Main).launch {
            fileOpeningViewModel.resetContainer()
            if (externalFileUris.isNotEmpty()) {
                navController.popBackStack()
            } else {
                navController.navigateUp()
            }
        }
    }

    LaunchedEffect(fileOpeningViewModel.filesAdded) {
        fileOpeningViewModel.filesAdded.asFlow().collect { files ->
            if (!files.isNullOrEmpty()) {
                val announcementText =
                    when (files.size) {
                        1 -> fileAddedText
                        else -> filesAddedText
                    }
                AccessibilityUtil.sendAccessibilityEvent(
                    context,
                    TYPE_ANNOUNCEMENT,
                    announcementText,
                )
                fileOpeningViewModel.resetFilesAdded()
            }
        }
    }

    LaunchedEffect(fileOpeningViewModel.signedContainer) {
        fileOpeningViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
                delay(2000)

                navController.navigate(Route.Signing.route) {
                    popUpTo(Route.Home.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(fileOpeningViewModel.launchFilePicker) {
        if (externalFileUris.isEmpty()) {
            fileOpeningViewModel.launchFilePicker.asFlow().collect { launchFilePicker ->
                launchFilePicker?.let {
                    if (it) {
                        fileOpeningViewModel.showFileChooser(filePicker)
                    }
                }
            }
        } else {
            externalFileUris.let { extFileUris ->
                fileUris = extFileUris
                isExternalFile = true
                CoroutineScope(IO).launch {
                    if (fileOpeningViewModel.isSivaConfirmationNeeded(extFileUris)) {
                        showSivaDialog.value = true
                    } else {
                        handleSivaConfirmation()
                    }
                }
            }
        }
    }

    SivaConfirmationDialog(
        showDialog = showSivaDialog,
        modifier = modifier,
        onResult = handleResult,
    )

    if (errorText.first != 0) {
        ToastUtil.DigiDocToast(
            modifier = modifier,
            message = context.getString(errorText.first, errorText.second),
        )
        errorText = Pair(0, null)
    }

    LoadingScreen(modifier = modifier)
}
