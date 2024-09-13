@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Black
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible

@Composable
fun CertificateDataItem(
    modifier: Modifier,
    @StringRes detailKey: Int,
    detailValue: String,
    certificate: Any? = null,
    contentDescription: String? = null,
    formatForAccessibility: Boolean = false,
    onCertificateButtonClick: () -> Unit = {},
) {
    val detailKeyText =
        if (detailKey != 0) {
            stringResource(id = detailKey)
        } else {
            ""
        }
    val buttonName = stringResource(id = R.string.button_name)

    val isClickable = certificate != null
    val contentDescriptionText =
        if (contentDescription.isNullOrEmpty()) {
            if (formatForAccessibility) {
                formatNumbers("$detailKeyText $detailValue").lowercase()
            } else {
                "$detailKeyText $detailValue".lowercase()
            }
        } else {
            if (formatForAccessibility) {
                formatNumbers(contentDescription).lowercase()
            } else {
                contentDescription.lowercase()
            }
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    if (isClickable) {
                        this.contentDescription = "$contentDescriptionText, $buttonName"
                    } else {
                        this.contentDescription = contentDescriptionText
                    }
                }
                .let {
                    if (isClickable) {
                        it.clickable(enabled = true, onClick = onCertificateButtonClick)
                    } else {
                        it
                    }
                },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConstraintLayout(
            modifier =
                modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(screenViewLargePadding)
                    .align(Alignment.CenterVertically),
        ) {
            val (
                dataItemColumn,
                dataItemIcon,
            ) = createRefs()
            Column(
                modifier =
                    modifier
                        .constrainAs(dataItemColumn) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
            ) {
                Text(
                    text = detailKeyText,
                    modifier = modifier.notAccessible(),
                )
                Text(
                    text = detailValue,
                    modifier = modifier.graphicsLayer(alpha = 0.7f).notAccessible(),
                )
            }
            if (certificate != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_keyboard_arrow_right_24),
                    contentDescription = null,
                    tint = Black,
                    modifier =
                        modifier
                            .padding(start = itemSpacingPadding)
                            .size(iconSize)
                            .notAccessible()
                            .constrainAs(dataItemIcon) {
                                end.linkTo(parent.end)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NFCViewPreview() {
    RIADigiDocTheme {
        CertificateDataItem(
            modifier = Modifier,
            detailKey = R.string.signers_certificate_label,
            detailValue = "John Doe".repeat(8),
            certificate = "null",
        )
    }
}
