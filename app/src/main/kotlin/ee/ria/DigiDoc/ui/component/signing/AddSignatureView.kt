@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@Composable
fun AddSignatureView(
    modifier: Modifier = Modifier,
    signatureAddController: NavHostController,
    dismissDialog: () -> Unit = {},
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val state =
        rememberScrollableState { delta ->
            focusManager.clearFocus()
            0f
        }

    Surface(
        modifier =
            modifier
                .wrapContentHeight()
                .wrapContentWidth()
                .padding(itemSpacingPadding)
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = state,
                )
                .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(screenViewLargePadding),
    ) {
        Column(
            modifier =
                modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .padding(itemSpacingPadding),
            // .offset { IntOffset(x = 0, y = state.) }
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SignatureAddRadioGroup(
                modifier = modifier,
                navController = signatureAddController,
                selectedRadioItem = sharedSettingsViewModel.dataStore.getSignatureAddMethod(),
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
            NavHost(
                navController = signatureAddController,
                startDestination = sharedSettingsViewModel.dataStore.getSignatureAddMethod(),
            ) {
                composable(route = Route.MobileId.route) {
                    MobileIdView(
                        cancelButtonClick = dismissDialog,
                        sharedContainerViewModel = sharedContainerViewModel,
                    )
                }
                composable(route = Route.SmartId.route) {
                    SmartIdView(
                        cancelButtonClick = dismissDialog,
                        sharedContainerViewModel = sharedContainerViewModel,
                    )
                }
                composable(route = Route.IdCard.route) {
                    IdCardView(cancelButtonClick = dismissDialog)
                }
                composable(route = Route.NFC.route) {
                    NFCView(cancelButtonClick = dismissDialog)
                }
            }
        }
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
            signatureAddController = signatureAddController,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
