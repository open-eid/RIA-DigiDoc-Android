@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.ui.component.signing.StyledNameText
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.libdigidoc.RecipientCertTypeUtil.getRecipientCertTypeText
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatCompanyName
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.utilsLib.date.DateUtil.dateFormat
import ee.ria.DigiDoc.utilsLib.validator.PersonalCodeValidator

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RecipientComponent(
    modifier: Modifier = Modifier,
    recipients: List<Addressee>,
    showRecipientsLoadingIndicator: Boolean,
    recipientsLoadingContentDescription: String,
    onClick: (Addressee) -> Unit,
    isCDOC2Container: Boolean = false,
) {
    val context = LocalContext.current
    val recipientText = stringResource(R.string.crypto_recipient_title)
    val buttonName = stringResource(id = R.string.button_name)

    if (showRecipientsLoadingIndicator) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(vertical = MPadding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier =
                    modifier
                        .size(loadingBarSize)
                        .semantics {
                            this.contentDescription = recipientsLoadingContentDescription
                        }
                        .testTag("recipientsLoadingProgress"),
            )
        }
    } else {
        Column {
            recipients.forEachIndexed { index, recipient ->
                val nameText =
                    if (PersonalCodeValidator.isPersonalCodeValid(recipient.identifier)) {
                        formatName(recipient.surname, recipient.givenName, recipient.identifier)
                    } else {
                        formatCompanyName(recipient.identifier, recipient.serialNumber)
                    }
                val certTypeText = getRecipientCertTypeText(LocalContext.current, recipient.certType)
                var certValidTo =
                    recipient.validTo?.let {
                        dateFormat.format(
                            it,
                        )
                    }?.let {
                        stringResource(
                            R.string.crypto_cert_valid_to,
                            it,
                        )
                    } ?: ""

                val decryptionValidToText =
                    if (isCDOC2Container) {
                        certValidTo = ""
                        recipient.validTo?.let {
                            dateFormat.format(
                                it,
                            )
                        }?.let {
                            stringResource(
                                R.string.crypto_decryption_valid_to,
                                it,
                            )
                        } ?: ""
                    } else {
                        ""
                    }

                Card(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTalkBackEnabled(context)) { onClick(recipient) }
                            .semantics {
                                this.contentDescription =
                                    "$recipientText ${index + 1} ${formatNumbers(nameText)} " +
                                    "$certTypeText $decryptionValidToText"
                                testTagsAsResourceId = true
                            }
                            .testTag("recipientComponentContainer"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = buttonRoundedCornerShape,
                ) {
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
                                val iconRes =
                                    if (recipient.surname.isNullOrEmpty() && recipient.givenName.isNullOrEmpty()) {
                                        R.drawable.ic_m3_domain_48dp_wght400
                                    } else {
                                        R.drawable.ic_m3_encrypted_48dp_wght400
                                    }
                                Icon(
                                    imageVector = ImageVector.vectorResource(iconRes),
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
                                            .testTag("recipientComponentIcon")
                                            .notAccessible(),
                                )
                            }

                            Spacer(modifier = modifier.width(SPadding))

                            Column(
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics(mergeDescendants = true) {
                                            testTagsAsResourceId = true
                                        }
                                        .focusGroup()
                                        .notAccessible(),
                            ) {
                                StyledNameText(
                                    modifier =
                                        modifier
                                            .focusable(false)
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("recipientComponentName"),
                                    name = nameText,
                                    formatName = false,
                                )
                                Text(
                                    text = "$certTypeText $certValidTo",
                                    modifier =
                                        modifier
                                            .focusable(false)
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("recipientComponentCert"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (decryptionValidToText.isNotEmpty()) {
                                    ColoredRecipientStatusText(
                                        text = decryptionValidToText,
                                        modifier =
                                            modifier
                                                .padding(vertical = SBorder)
                                                .focusable(false)
                                                .notAccessible(),
                                    )
                                }
                            }

                            IconButton(onClick = { onClick(recipient) }) {
                                Icon(
                                    modifier =
                                        modifier
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("recipientComponentMoreOptionsIconButton"),
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_more_vert),
                                    contentDescription = "$recipientText ${index + 1} ${stringResource(
                                        R.string.more_options,
                                    )} $buttonName",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
