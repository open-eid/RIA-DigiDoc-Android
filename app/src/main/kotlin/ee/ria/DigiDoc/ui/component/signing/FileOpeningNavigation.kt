@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.FileOpeningViewModel
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val errorState by fileOpeningViewModel.errorState.asFlow().collectAsState(null)
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
            onResult = { uri ->
                CoroutineScope(Dispatchers.IO).launch {
                    fileOpeningViewModel.handleFiles(uri, signedContainer)

                    withContext(Dispatchers.Main) {
                        errorState?.let {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    }
                }
            },
        )

    LaunchedEffect(fileOpeningViewModel.errorState) {
        fileOpeningViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
            }
        }
    }

    BackHandler {
        navController.popBackStack()
    }

    LaunchedEffect(fileOpeningViewModel.signedContainer) {
        fileOpeningViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
                navController.navigate(Route.Signing.route)
            }
        }
    }

    LaunchedEffect(fileOpeningViewModel.launchFilePicker) {
        if (fileOpeningViewModel.launchFilePicker.value) {
            fileOpeningViewModel.showFileChooser(filePicker)
        }
    }

    Scaffold { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusGroup(),
        ) {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(vertical = screenViewVerticalPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = modifier.size(loadingBarSize),
                )
            }
        }
    }
}
