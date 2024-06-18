@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.info

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.LinkifyText
import ee.ria.DigiDoc.ui.theme.Blue500
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
    Text(
        modifier =
            modifier.padding(
                horizontal = screenViewLargePadding,
                vertical = screenViewLargePadding,
            ),
        text = stringResource(name),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight(700),
    )
    LinkifyText(
        modifier =
            modifier.padding(
                horizontal = screenViewLargePadding,
                vertical = screenViewSmallPadding,
            ),
        text = stringResource(licenseName),
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER,
        textColor = MaterialTheme.colorScheme.primary.toArgb(),
        linkTextColor = Blue500.toArgb(),
    )
    LinkifyText(
        modifier =
            modifier.padding(
                horizontal = screenViewLargePadding,
                vertical = screenViewSmallPadding,
            ),
        text = stringResource(licenseUrl),
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER,
        textColor = MaterialTheme.colorScheme.primary.toArgb(),
        linkTextColor = Blue500.toArgb(),
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureAddRadioButtonPreview() {
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
