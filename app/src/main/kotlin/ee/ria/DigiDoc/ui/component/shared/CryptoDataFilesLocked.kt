@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CryptoDataFilesLocked(modifier: Modifier = Modifier) {
    val textDescription = stringResource(id = R.string.crypto_files_encrypted)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SPadding)
                .padding(start = SPadding, end = XSPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(),
        ) {
            Box(
                modifier =
                    modifier
                        .wrapContentHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector =
                        ImageVector.vectorResource(
                            id = R.drawable.ic_m3_encrypted_48dp_wght400,
                        ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        modifier
                            .padding(XSPadding)
                            .size(iconSizeXXS)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("dataFileItemIconEncrypted")
                            .notAccessible(),
                )
            }

            Spacer(modifier = modifier.width(SPadding))

            Column(modifier = modifier.weight(1f)) {
                Text(
                    modifier =
                        modifier
                            .padding(zeroPadding)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .focusable(false)
                            .semantics {
                                this.contentDescription = textDescription
                                testTagsAsResourceId = true
                            }
                            .testTag("dataFileItemEncrypted"),
                    text = textDescription,
                )
            }
        }
        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CryptoDataFilesLockedPreview() {
    RIADigiDocTheme {
        CryptoDataFilesLocked()
    }
}
