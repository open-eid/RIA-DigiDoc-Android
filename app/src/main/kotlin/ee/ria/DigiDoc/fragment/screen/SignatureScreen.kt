@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SignatureScreen(
    modifier: Modifier = Modifier,
    onClickToFileChoosingScreen: () -> Unit = {},
) {
    RIADigiDocTheme {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = screenViewHorizontalPadding,
                        vertical = screenViewVerticalPadding,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(id = R.string.signature_home_create_text),
                modifier =
                    modifier
                        .focusable(false)
                        .clearAndSetSemantics {},
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                title = R.string.signature_home_create_button,
                contentDescription = stringResource(id = R.string.signature_home_create_text),
                onClickItem = onClickToFileChoosingScreen,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureScreenPreview() {
    RIADigiDocTheme {
        SignatureScreen()
    }
}
