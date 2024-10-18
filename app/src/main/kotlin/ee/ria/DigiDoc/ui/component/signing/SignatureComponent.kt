@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil.getSignatureStatusText
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.viewmodel.SigningViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignatureComponent(
    modifier: Modifier = Modifier,
    signingViewModel: SigningViewModel,
    signature: SignatureInterface,
    showRemoveButton: Boolean,
    onRemoveButtonClick: () -> Unit = {},
    showRolesDetailsButton: Boolean,
    onRolesDetailsButtonClick: () -> Unit = {},
    onSignerDetailsButtonClick: () -> Unit = {},
    testTag: String = "",
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = screenViewLargePadding)
                .semantics(mergeDescendants = true) {
                    testTagsAsResourceId = true
                }
                .focusGroup()
                .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_signature),
            contentDescription = null,
            modifier =
                modifier
                    .size(iconSize)
                    .focusable(false)
                    .testTag("signatureUpdateListSignatureType"),
        )
        Spacer(modifier = modifier.width(itemSpacingPadding))

        val nameText = formatName(signature.name)
        val statusText =
            getSignatureStatusText(
                LocalContext.current,
                signature.validator.status,
            )
        val signedTime =
            stringResource(
                R.string.signing_container_signature_created_at,
                signingViewModel.getFormattedDate(signature.trustedSigningTime),
            )
        val buttonName = stringResource(id = R.string.button_name)
        val roles = signature.signerRoles.joinToString(" / ")
        val roleAndAddress = stringResource(R.string.signature_update_signature_role_and_address_title_accessibility)
        Column(
            modifier =
                modifier
                    .semantics(mergeDescendants = true) {
                        testTagsAsResourceId = true
                        this.contentDescription =
                            "${formatNumbers(nameText)}, $statusText, $roleAndAddress: $roles, $signedTime, $buttonName"
                    }
                    .weight(1f)
                    .focusGroup()
                    .clickable(onClick = onSignerDetailsButtonClick),
        ) {
            Text(
                text = nameText,
                modifier =
                    modifier
                        .focusable(false)
                        .testTag("signatureUpdateListSignatureName"),
                fontWeight = FontWeight.Bold,
            )
            ColoredSignedStatusText(
                text = statusText,
                status = signature.validator.status,
                modifier =
                    modifier
                        .focusable(false),
            )
            if (showRolesDetailsButton) {
                Text(
                    text = roles,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        modifier
                            .focusable(false)
                            .testTag("signatureUpdateListSignatureRole"),
                )
            }
            Text(
                text = signedTime,
                modifier =
                    modifier
                        .focusable(false)
                        .testTag("signatureUpdateListSignatureCreatedAt"),
            )
        }
        if (showRolesDetailsButton) {
            IconButton(
                onClick = onRolesDetailsButtonClick,
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_info),
                        contentDescription = roleAndAddress,
                        tint = Blue500,
                    )
                },
                modifier =
                    modifier
                        .size(iconSize)
                        .testTag("signatureUpdateListSignatureRoleDetailsButton"),
            )
        }
        Spacer(modifier = modifier.width(itemSpacingPadding))
        if (showRemoveButton) {
            IconButton(
                onClick = onRemoveButtonClick,
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = "${
                            stringResource(
                                id = R.string.signature_remove_button,
                            )
                        } ${formatNumbers(signature.signedBy)}",
                        tint = Red500,
                    )
                },
                modifier =
                    modifier
                        .size(iconSize)
                        .testTag("signatureUpdateListSignatureRemoveButton"),
            )
        }
    }
}
