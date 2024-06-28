@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@Composable
fun NFCView(
    modifier: Modifier = Modifier,
    cancelButtonClick: () -> Unit = {},
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Signature with NFC",
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = screenViewLargePadding),
            textAlign = TextAlign.Center,
        )
        val loading by remember { mutableStateOf(true) }

        if (!loading) return

        // TODO: Enable when implemented

        /*CircularProgressIndicator(
            modifier = modifier.size(loadingBarSize),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        CancelAndOkButtonRow(
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.sign_button).lowercase(),
            cancelButtonClick = cancelButtonClick,
            okButtonClick = {
                // TODO:
            },
        )*/
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NFCViewPreview() {
    RIADigiDocTheme {
        NFCView()
    }
}
