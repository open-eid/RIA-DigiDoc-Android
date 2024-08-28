@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.LoadingScreen
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.viewmodel.FileOpeningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
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
    val externalFileUri by sharedContainerViewModel.externalFileUri.collectAsState()
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
            onResult = { uri ->
                if (uri.isEmpty()) {
                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }
                CoroutineScope(IO).launch {
                    fileOpeningViewModel.handleFiles(uri, signedContainer)
                }
            },
        )

    val fileAddedText = stringResource(id = R.string.file_added)
    val filesAddedText = stringResource(id = R.string.files_added)

    LaunchedEffect(fileOpeningViewModel.errorState) {
        fileOpeningViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
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
            if (externalFileUri != null) {
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
        if (externalFileUri == null) {
            fileOpeningViewModel.launchFilePicker.asFlow().collect { launchFilePicker ->
                launchFilePicker?.let {
                    if (it) {
                        fileOpeningViewModel.showFileChooser(filePicker)
                    }
                }
            }
        } else {
            externalFileUri?.let { fileUri ->
                CoroutineScope(IO).launch {
                    fileOpeningViewModel.handleFiles(listOf(fileUri))
                }
            }
        }
    }

    LoadingScreen()
}
