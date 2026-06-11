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

package ee.ria.DigiDoc.ui.component.signing

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.common.model.FileOpeningMethod
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.ui.component.shared.dialog.SivaConfirmationDialog
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.FileOpeningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FileOpeningNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    fileOpeningMethod: FileOpeningMethod,
    fileOpeningViewModel: FileOpeningViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val cryptoContainer by sharedContainerViewModel.cryptoContainer.asFlow().collectAsState(null)
    val externalFileUris by sharedContainerViewModel.externalFileUris.collectAsState()
    val showSivaDialog = rememberSaveable { mutableStateOf(false) }
    var isExternalFile by rememberSaveable { mutableStateOf(false) }
    var fileUris by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }

    val handleSivaConfirmation: () -> Unit = {
        showSivaDialog.value = false
        scope.launch(Main) {
            sharedContainerViewModel.setIsSivaConfirmed(true)
        }
        fileOpeningViewModel.resetExternalFileState(sharedContainerViewModel)
        scope.launch(IO) {
            fileOpeningViewModel.handleFiles(
                context,
                fileUris,
                signedContainer,
                cryptoContainer,
                true,
                false,
                isExternalFile,
                fileOpeningMethod,
            )
            fileUris = emptyList()
        }
    }

    val handleSivaCancel: () -> Unit = {
        showSivaDialog.value = false
        scope.launch(Main) {
            sharedContainerViewModel.setIsSivaConfirmed(false)
        }
        fileOpeningViewModel.resetExternalFileState(sharedContainerViewModel)
        scope.launch(IO) {
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

                scope.launch(IO) {
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
                    delay(4000)
                    if (signedContainer == null) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    BackHandler {
        scope.launch(Main) {
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

                sharedContainerViewModel.setAddedFilesCount(files.size)

                sendAccessibilityEvent(
                    context,
                    getAccessibilityEventType(),
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
                delay(1000)

                navController.navigate(Route.Signing.route) {
                    popUpTo(Route.Home.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(fileOpeningViewModel.cryptoContainer) {
        fileOpeningViewModel.cryptoContainer.asFlow().collect { cryptoContainer ->
            cryptoContainer?.let {
                sharedContainerViewModel.setCryptoContainer(it)

                navController.navigate(Route.Encrypt.route) {
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
                scope.launch(IO) {
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
        showMessage(context.getString(errorText.first, errorText.second))
        errorText = Pair(0, null)
    }

    LoadingScreen(modifier = modifier)
}
