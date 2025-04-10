@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

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
import androidx.compose.ui.text.style.TextOverflow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
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
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil.getSignatureStatusText
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil.getTimestampStatusText
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.utilsLib.date.DateUtil

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignatureComponent(
    modifier: Modifier = Modifier,
    isTimestamped: Boolean = false,
    signatures: List<SignatureInterface>,
    showSignaturesLoadingIndicator: Boolean,
    signaturesLoadingContentDescription: String,
    onClick: (SignatureInterface) -> Unit,
) {
    val context = LocalContext.current
    val signatureText = stringResource(R.string.signature_details_signer_details_title)

    val buttonName = stringResource(id = R.string.button_name)

    if (showSignaturesLoadingIndicator) {
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
                            this.contentDescription = signaturesLoadingContentDescription
                        }
                        .testTag("signaturesLoadingProgress"),
            )
        }
    } else {
        Column {
            signatures.forEachIndexed { index, signature ->
                val nameText = formatName(signature.name)
                val statusText =
                    if (isTimestamped) {
                        getTimestampStatusText(LocalContext.current, signature.validator.status)
                    } else {
                        getSignatureStatusText(LocalContext.current, signature.validator.status)
                    }
                val signedTime =
                    stringResource(
                        R.string.signing_container_signature_created_at,
                        DateUtil.formattedDateTime(signature.trustedSigningTime).date,
                        DateUtil.formattedDateTime(signature.trustedSigningTime).time,
                    )
                val roles = signature.signerRoles.joinToString(" / ")
                val roleAndAddress =
                    stringResource(R.string.signature_update_signature_role_and_address_title)

                Card(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTalkBackEnabled(context)) { onClick(signature) }
                            .semantics {
                                if (roles.isEmpty()) {
                                    this.contentDescription =
                                        "$signatureText ${index + 1} ${formatNumbers(nameText)} $statusText $signedTime"
                                } else {
                                    this.contentDescription =
                                        "${formatNumbers(nameText)}, $statusText, $signedTime, $roleAndAddress: $roles"
                                }
                                testTagsAsResourceId = true
                            }
                            .testTag("signatureComponentContainer"),
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
                                Icon(
                                    imageVector =
                                        if (isTimestamped) {
                                            ImageVector.vectorResource(R.drawable.ic_m3_approval_48dp_wght400)
                                        } else {
                                            ImageVector.vectorResource(R.drawable.ic_icon_signature)
                                        },
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
                                            .testTag("signatureComponentIcon")
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
                                            .testTag("signatureComponentSignatureName"),
                                    name = nameText,
                                    allCaps = isTimestamped,
                                )
                                Text(
                                    text = signedTime,
                                    modifier =
                                        modifier
                                            .focusable(false)
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("signatureComponentSignatureCreatedAt"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                ColoredSignedStatusText(
                                    text = statusText,
                                    status = signature.validator.status,
                                    modifier =
                                        modifier
                                            .padding(vertical = SBorder)
                                            .focusable(false)
                                            .notAccessible(),
                                )
                                if (!signature.signerRoles.isEmpty()) {
                                    Text(
                                        text = roles,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier =
                                            modifier
                                                .focusable(false)
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("signatureComponentSignatureRole"),
                                    )
                                }
                            }

                            IconButton(onClick = { onClick(signature) }) {
                                Icon(
                                    modifier =
                                        modifier
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("signatureComponentMoreOptionsIconButton"),
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_more_vert),
                                    contentDescription = "$signatureText ${index + 1} ${stringResource(
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
