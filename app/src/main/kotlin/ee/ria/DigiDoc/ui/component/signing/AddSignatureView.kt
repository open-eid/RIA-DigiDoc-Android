@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.FileOpeningViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddSignatureView(
    activity: Activity,
    modifier: Modifier = Modifier,
    signatureAddController: NavHostController,
    dismissDialog: () -> Unit = {},
    cancelButtonClick: () -> Unit = {},
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    fileOpeningViewModel: FileOpeningViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val state =
        rememberScrollableState { _ ->
            focusManager.clearFocus()
            0f
        }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        sharedContainerViewModel.signedContainer.asFlow().collect { signedContainer ->
            if (signedContainer?.isLegacy() == true) {
                fileOpeningViewModel.resetExternalFileState(sharedContainerViewModel)
                CoroutineScope(IO).launch {
                    fileOpeningViewModel.handleFiles(
                        context = context,
                        uris = listOf(Uri.fromFile(signedContainer.getContainerFile())),
                        existingSignedContainer = null,
                        isSivaConfirmed = true,
                        forceFirstDataFileContainer = true,
                    )
                }
            }
        }
    }

    LaunchedEffect(fileOpeningViewModel.signedContainer) {
        fileOpeningViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
            }
        }
    }

    Surface(
        modifier =
            modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .padding(itemSpacingPadding)
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = state,
                )
                .semantics {
                    testTagsAsResourceId = true
                }
                .verticalScroll(scrollState),
        shape = RoundedCornerShape(screenViewLargePadding),
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxHeight()
                    .wrapContentWidth()
                    .padding(itemSpacingPadding)
                    .testTag("addSignatureView"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            NavHost(
                navController = signatureAddController,
                startDestination = sharedSettingsViewModel.dataStore.getSignatureAddMethod(),
            ) {
                composable(route = Route.MobileId.route) {
                    MobileIdView(
                        activity = activity,
                        dismissDialog = dismissDialog,
                        cancelButtonClick = cancelButtonClick,
                        signatureAddController = signatureAddController,
                        sharedContainerViewModel = sharedContainerViewModel,
                    )
                }
                composable(route = Route.SmartId.route) {
                    SmartIdView(
                        activity = activity,
                        dismissDialog = dismissDialog,
                        cancelButtonClick = cancelButtonClick,
                        signatureAddController = signatureAddController,
                        sharedContainerViewModel = sharedContainerViewModel,
                    )
                }
                composable(route = Route.IdCard.route) {
                    IdCardView(
                        activity = activity,
                        cancelButtonClick = cancelButtonClick,
                        dismissDialog = dismissDialog,
                        signatureAddController = signatureAddController,
                        sharedContainerViewModel = sharedContainerViewModel,
                    )
                }
                composable(route = Route.NFC.route) {
                    NFCView(
                        activity = activity,
                        dismissDialog = dismissDialog,
                        cancelButtonClick = cancelButtonClick,
                        signatureAddController = signatureAddController,
                        sharedContainerViewModel = sharedContainerViewModel,
                    )
                }
            }
        }
        InvisibleElement(modifier = modifier)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddSignatureViewPreview() {
    val signatureAddController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        AddSignatureView(
            activity = LocalContext.current as Activity,
            signatureAddController = signatureAddController,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
