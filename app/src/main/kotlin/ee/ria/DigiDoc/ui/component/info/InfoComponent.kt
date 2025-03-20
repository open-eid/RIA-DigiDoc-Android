@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.info

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InfoComponent(
    modifier: Modifier = Modifier,
    @StringRes name: Int,
    @StringRes licenseName: Int,
    @StringRes licenseUrl: Int,
) {
    val context = LocalContext.current
    val licenseUrlString = stringResource(id = licenseUrl)
    val openButtonContentDescription = "${stringResource(R.string.open_button)} $licenseUrlString"
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = XSPadding)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("mainAboutComponentView"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                modifier
                    .weight(1f)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                modifier =
                    modifier
                        .padding(
                            horizontal = MPadding,
                        )
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("mainAboutComponentNameText"),
                text = stringResource(name),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight(700),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            horizontal = MPadding,
                        )
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("mainAboutComponentLicenseNameText"),
                text = stringResource(licenseName),
                textStyle =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                    ),
            )
        }

        IconButton(
            onClick = {
                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(licenseUrlString),
                    )

                context.startActivity(browserIntent, null)
            },
            modifier =
                modifier
                    .padding(horizontal = SPadding),
        ) {
            Icon(
                modifier =
                    modifier
                        .size(iconSizeXXS)
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("mainAboutComponentLicenseUrlButton"),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_open_in_new_48dp_wght400),
                contentDescription = openButtonContentDescription,
            )
        }
    }
    HorizontalDivider(
        modifier =
            modifier
                .padding(SPadding),
    )
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
