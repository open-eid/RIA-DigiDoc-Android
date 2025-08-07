@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.IdentityAction
import ee.ria.DigiDoc.ui.component.signing.NFCView
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.WebEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@Composable
fun WebEidScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: WebEidViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel = hiltViewModel(),
) {
    val noAuthLabel = stringResource(id = R.string.web_eid_auth_no_payload)
    val activity = LocalActivity.current as Activity
    val authPayload = viewModel.authPayload.collectAsState().value

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { testTagsAsResourceId = true }
                .testTag("webEidScreen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (authPayload != null) {
                NFCView(
                    activity = activity,
                    identityAction = IdentityAction.AUTH,
                    isSigning = false,
                    isDecrypting = false,
                    isAuthenticating = false,
                    onError = {},
                    onSuccess = {},
                    sharedSettingsViewModel = sharedSettingsViewModel,
                    sharedContainerViewModel = sharedContainerViewModel,
                    isSupported = {},
                    isValidToSign = {},
                    isValidToDecrypt = {},
                    isValidToAuthenticate = {},
                    isAuthenticated = { _, _ -> },
                    webEidViewModel = viewModel,
                )
            } else {
                Text(noAuthLabel)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WebEidScreenPreview() {
    RIADigiDocTheme {
        WebEidScreen(
            navController = rememberNavController(),
            viewModel = hiltViewModel(),
        )
    }
}
