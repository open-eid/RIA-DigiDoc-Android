@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.SettingsViewModel
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel

@Composable
fun AddSignatureView(
    modifier: Modifier = Modifier,
    signatureAddController: NavHostController,
    dismissDialog: () -> Unit = {},
    sharedContainerViewModel: SharedContainerViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val signatureAddMethod = settingsViewModel.dataStore.getSignatureAddMethod()
    Surface(
        modifier =
            modifier
                .wrapContentHeight()
                .wrapContentWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.alertDialogOuterPadding),
    ) {
        Column(
            modifier =
                modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .padding(Dimensions.alertDialogInnerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SignatureAddRadioGroup(
                modifier = modifier,
                navController = signatureAddController,
                selectedRadioItem = signatureAddMethod,
                settingsViewModel = settingsViewModel,
            )
            NavHost(
                navController = signatureAddController,
                startDestination = signatureAddMethod,
            ) {
                composable(route = Route.MobileId.route) {
                    MobileIdView(
                        cancelButtonClick = dismissDialog,
                        sharedContainerViewModel = sharedContainerViewModel,
                        // TODO: Inject configurationViewModel
                        configurationViewModel = hiltViewModel(),
                    )
                }
                composable(route = Route.SmartId.route) {
                    SmartIdView(cancelButtonClick = dismissDialog)
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
