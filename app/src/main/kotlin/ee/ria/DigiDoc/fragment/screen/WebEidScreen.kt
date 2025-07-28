@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.WebEidViewModel

@Composable
fun WebEidScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController, // TODO navController is not yet used; reserved for navigation after auth completes
    viewModel: WebEidViewModel,
) {
    val auth = viewModel.authPayload.collectAsState().value
    val challengeLabel = stringResource(id = R.string.web_eid_auth_label_challenge)
    val loginUriLabel = stringResource(id = R.string.web_eid_auth_label_login_uri)
    val getCertLabel = stringResource(id = R.string.web_eid_auth_label_get_signing_cert)
    val noAuthLabel = stringResource(id = R.string.web_eid_auth_no_payload)

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { testTagsAsResourceId = true }
                .testTag("webEidScreen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (auth != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$challengeLabel: ${auth.challenge}")
                    Text("$loginUriLabel: ${auth.loginUri}")
                    Text("$getCertLabel: ${auth.getSigningCertificate}")
                }
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