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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil.getSignatureStatusText
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.viewmodel.SigningViewModel

@Composable
fun SignatureComponent(
    modifier: Modifier = Modifier,
    signingViewModel: SigningViewModel,
    signature: SignatureInterface,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(screenViewHorizontalPadding)
                .semantics(mergeDescendants = true) {}
                .focusGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_signature),
            contentDescription = null,
            modifier =
                modifier
                    .size(Dimensions.iconSize)
                    .focusable(false)
                    .clearAndSetSemantics {},
        )
        Spacer(modifier = modifier.width(itemSpacingPadding))
        Column(
            modifier =
                modifier
                    .semantics(mergeDescendants = true) {}
                    .weight(1f)
                    .focusGroup()
                    .clickable { /* TODO */ },
        ) {
            Text(
                text = formatName(signature.signedBy),
                modifier = modifier.focusable(),
                fontWeight = FontWeight.Bold,
            )
            ColoredSignedStatusText(
                text =
                    getSignatureStatusText(
                        LocalContext.current,
                        signature.validator.status,
                    ),
                status = signature.validator.status,
                modifier = modifier,
            )
            Text(
                text =
                    stringResource(
                        R.string.signing_container_signature_created_at,
                        signingViewModel.getFormattedDate(signature.trustedSigningTime),
                    ),
                modifier = modifier.focusable(),
            )
        }
        IconButton(
            onClick = { /* TODO */ },
            content = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                    contentDescription = "${stringResource(
                        id = R.string.signature_remove_button,
                    )} ${signature.signedBy}",
                    tint = Red500,
                )
            },
            modifier =
                modifier
                    .size(Dimensions.iconSize),
        )
    }
}
