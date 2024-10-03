@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.info

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun InfoComponent(
    modifier: Modifier = Modifier,
    @StringRes name: Int,
    @StringRes licenseName: Int,
    @StringRes licenseUrl: Int,
) {
    Column(
        modifier = modifier.padding(vertical = screenViewSmallPadding).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier =
                modifier.padding(
                    horizontal = screenViewLargePadding,
                ),
            text = stringResource(name),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight(700),
        )
        DynamicText(
            modifier =
                modifier.padding(
                    horizontal = screenViewLargePadding,
                ),
            text = stringResource(licenseName),
            textStyle =
                TextStyle(
                    textAlign = TextAlign.Center,
                ),
        )
        DynamicText(
            modifier =
                modifier.padding(
                    horizontal = screenViewLargePadding,
                ),
            text = stringResource(licenseUrl),
            textStyle =
                TextStyle(
                    textAlign = TextAlign.Center,
                ),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InfoComponentPreview() {
    RIADigiDocTheme {
        Column {
            InfoComponent(
                name = R.string.main_about_bouncy_castle_title,
                licenseName = R.string.main_about_mit_license_title,
                licenseUrl = R.string.main_about_mit_license_url,
            )
            InfoComponent(
                name = R.string.main_about_square_okhttp_title,
                licenseName = R.string.main_about_apache_2_license_title,
                licenseUrl = R.string.main_about_apache_2_license_url,
            )
        }
    }
}
