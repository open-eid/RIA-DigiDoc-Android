@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Black
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.utils.extensions.notAccessible

@Composable
fun CertificateDataItem(
    modifier: Modifier,
    @StringRes detailKey: Int,
    detailValue: String,
    certificate: Any? = null,
    contentDescription: String? = null,
    onCertificateButtonClick: () -> Unit = {},
) {
    val detailKeyText =
        if (detailKey != 0) {
            stringResource(id = detailKey)
        } else {
            ""
        }
    val buttonName = stringResource(id = R.string.button_name)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = screenViewLargePadding)
                .semantics(mergeDescendants = true) {}
                .focusGroup()
                .clickable(enabled = certificate != null, onClick = onCertificateButtonClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                modifier
                    .semantics(mergeDescendants = true) {}
                    .weight(1f)
                    .focusGroup(),
        ) {
            Text(
                text = detailKeyText,
                modifier =
                    modifier
                        .focusable()
                        .semantics {
                            this.contentDescription = contentDescription ?: detailValue
                        },
            )
            Text(
                text = detailValue,
                modifier =
                    modifier
                        .graphicsLayer(alpha = 0.7f)
                        .focusable(),
            )
        }
        if (certificate != null) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_keyboard_arrow_right_24),
                contentDescription = null,
                tint = Black,
                modifier =
                    modifier
                        .size(iconSize)
                        .notAccessible(),
            )
        }
    }
}
