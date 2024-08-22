@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@Composable
fun RolesDetailsView(
    navController: NavController,
    modifier: Modifier = Modifier,
    sharedSignatureViewModel: SharedSignatureViewModel,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    val signature = sharedSignatureViewModel.signature.value

    BackHandler {
        handleBackButtonClick(navController, sharedSignatureViewModel)
    }

    Scaffold { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .focusGroup(),
        ) {
            Column {
                TopBar(
                    modifier = modifier,
                    title = R.string.signature_update_signature_role_and_address_title_accessibility,
                    onBackButtonClick = {
                        handleBackButtonClick(navController, sharedSignatureViewModel)
                    },
                )
                if (signature != null) {
                    Text(
                        text = stringResource(id = R.string.main_settings_role_title),
                        modifier = modifier.notAccessible(),
                    )
                    Text(
                        text = signature.signerRoles.joinToString(", "),
                        modifier =
                            modifier
                                .graphicsLayer(alpha = 0.7f)
                                .notAccessible(),
                    )
                    Text(
                        text = stringResource(id = R.string.main_settings_city_title),
                        modifier = modifier.notAccessible(),
                    )
                    Text(
                        text = signature.city,
                        modifier =
                            modifier
                                .graphicsLayer(alpha = 0.7f)
                                .notAccessible(),
                    )
                    Text(
                        text = stringResource(id = R.string.main_settings_county_title),
                        modifier = modifier.notAccessible(),
                    )
                    Text(
                        text = signature.stateOrProvince,
                        modifier =
                            modifier
                                .graphicsLayer(alpha = 0.7f)
                                .notAccessible(),
                    )
                    Text(
                        text = stringResource(id = R.string.main_settings_country_title),
                        modifier = modifier.notAccessible(),
                    )
                    Text(
                        text = signature.countryName,
                        modifier =
                            modifier
                                .graphicsLayer(alpha = 0.7f)
                                .notAccessible(),
                    )
                    Text(
                        text = stringResource(id = R.string.main_settings_postal_code_title),
                        modifier = modifier.notAccessible(),
                    )
                    Text(
                        text = signature.postalCode,
                        modifier =
                            modifier
                                .graphicsLayer(alpha = 0.7f)
                                .notAccessible(),
                    )
                }
            }
        }
    }
}

private fun handleBackButtonClick(
    navController: NavController,
    sharedSignatureViewModel: SharedSignatureViewModel,
) {
    sharedSignatureViewModel.resetSignature()
    navController.navigateUp()
}
