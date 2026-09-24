// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.CryptoFileOpeningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CryptoFileOpeningNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    cryptoFileOpeningViewModel: CryptoFileOpeningViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current

    val cryptoContainer by sharedContainerViewModel.cryptoContainer.collectAsState()
    val externalFileUris by sharedContainerViewModel.externalFileUris.collectAsState()
    var isExternalFile by remember { mutableStateOf(false) }
    var fileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val handleFileOpening: () -> Unit = {
        cryptoFileOpeningViewModel.resetExternalFileState(sharedContainerViewModel)
        CoroutineScope(IO).launch {
            cryptoFileOpeningViewModel.handleFiles(context, fileUris, cryptoContainer)
            fileUris = emptyList()
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
                    handleFileOpening()
                }
            },
        )

    val fileAddedText = stringResource(id = R.string.file_added)
    val filesAddedText = stringResource(id = R.string.files_added)
    var errorText by remember { mutableStateOf(Pair<Int, String?>(0, null)) }

    LaunchedEffect(cryptoFileOpeningViewModel.errorState) {
        cryptoFileOpeningViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    if (errorState.first != 0) {
                        errorText = errorState
                    }
                    delay(1000)
                    if (cryptoContainer == null) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    BackHandler {
        CoroutineScope(Main).launch {
            cryptoFileOpeningViewModel.resetContainer()
            if (externalFileUris.isNotEmpty()) {
                navController.popBackStack()
            } else {
                navController.navigateUp()
            }
        }
    }

    LaunchedEffect(cryptoFileOpeningViewModel.filesAdded) {
        cryptoFileOpeningViewModel.filesAdded.asFlow().collect { files ->
            if (!files.isNullOrEmpty()) {
                val announcementText =
                    when (files.size) {
                        1 -> fileAddedText
                        else -> filesAddedText
                    }

                sharedContainerViewModel.setAddedFilesCount(files.size)

                sendAccessibilityEvent(
                    context,
                    announcementText,
                )
                delay(1000)
                cryptoFileOpeningViewModel.resetFilesAdded()
            }
        }
    }

    LaunchedEffect(cryptoFileOpeningViewModel.cryptoContainer) {
        cryptoFileOpeningViewModel.cryptoContainer.asFlow().collect { cryptoContainer ->
            cryptoContainer?.let {
                sharedContainerViewModel.setCryptoContainer(it)
                delay(1000)

                navController.navigate(Route.Encrypt.route) {
                    popUpTo(Route.Home.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(cryptoFileOpeningViewModel.launchFilePicker) {
        if (externalFileUris.isEmpty()) {
            cryptoFileOpeningViewModel.launchFilePicker.asFlow().collect { launchFilePicker ->
                launchFilePicker?.let {
                    if (it) {
                        cryptoFileOpeningViewModel.showFileChooser(filePicker)
                    }
                }
            }
        } else {
            externalFileUris.let { extFileUris ->
                fileUris = extFileUris
                isExternalFile = true
                CoroutineScope(IO).launch {
                    handleFileOpening()
                }
            }
        }
    }

    if (errorText.first != 0) {
        showMessage(context.getString(errorText.first, errorText.second))
        errorText = Pair(0, null)
    }

    LoadingScreen(modifier = modifier)
}
