@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignatureDataItem(
    modifier: Modifier,
    @DrawableRes icon: Int,
    @StringRes detailKey: Int,
    detailValue: String,
    certificate: Any? = null,
    isLink: Boolean = false,
    contentDescription: String? = null,
    formatForAccessibility: Boolean = false,
    onCertificateButtonClick: () -> Unit = {},
    testTag: String = "",
) {
    val detailKeyText =
        if (detailKey != 0) {
            stringResource(id = detailKey)
        } else {
            ""
        }
    val linkName = stringResource(id = R.string.link)
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }

    val isWithCertificate = certificate != null
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

    val buttonName = stringResource(id = R.string.button_name)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .focusable(true, interactionSource)
                .indication(interactionSource, LocalIndication.current)
                .semantics(mergeDescendants = true) {
                    if (isLink) {
                        this.contentDescription = "$contentDescriptionText, $linkName"
                    } else if (isWithCertificate) {
                        this.contentDescription = "$contentDescriptionText, $buttonName"
                    } else {
                        this.contentDescription = contentDescriptionText
                    }
                }.let {
                    if (isLink) {
                        it.clickable(enabled = true, onClick = { uriHandler.openUri(detailValue) })
                    } else if (isWithCertificate) {
                        it.clickable(enabled = true, onClick = onCertificateButtonClick)
                    } else {
                        it
                    }
                },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                modifier
                    .weight(1f)
                    .padding(vertical = SPadding),
        ) {
            Text(
                text = detailKeyText,
                modifier =
                    modifier
                        .focusable(false)
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag(testTag + "Title")
                        .notAccessible(),
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = detailValue,
                modifier =
                    modifier
                        .focusable(false)
                        .testTag(testTag)
                        .notAccessible(),
                textAlign = TextAlign.Start,
            )
        }

        if (icon != 0) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier =
                    modifier
                        .size(iconSizeXXS)
                        .focusable(false)
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag(testTag + "Button")
                        .notAccessible(),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureDataItemPreview() {
    RIADigiDocTheme {
        SignatureDataItem(
            modifier = Modifier,
            icon = R.drawable.ic_m3_expand_content_48dp_wght400,
            detailKey = R.string.signers_certificate_label,
            detailValue = "John Doe".repeat(8),
            certificate = "null",
        )
    }
}
