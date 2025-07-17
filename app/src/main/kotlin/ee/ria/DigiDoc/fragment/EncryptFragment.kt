@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.EncryptScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EncryptFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedRecipientViewModel: SharedRecipientViewModel,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("encryptFragment"),
        color = MaterialTheme.colorScheme.background,
    ) {
        EncryptScreen(
            navController = navController,
            modifier = modifier,
            sharedMenuViewModel = sharedMenuViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedRecipientViewModel = sharedRecipientViewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptFragmentPreview() {
    RIADigiDocTheme {
        EncryptFragment(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedContainerViewModel = hiltViewModel(),
            sharedRecipientViewModel = hiltViewModel(),
        )
    }
}
