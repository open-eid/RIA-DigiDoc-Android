@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.theme.Green_2_50
import ee.ria.DigiDoc.ui.theme.Green_2_700
import ee.ria.DigiDoc.ui.theme.Red50
import ee.ria.DigiDoc.ui.theme.Red800
import ee.ria.DigiDoc.ui.theme.Yellow800

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColoredSignedStatusText(
    text: String,
    status: ValidatorInterface.Status,
    modifier: Modifier = Modifier,
) {
    val parts = text.split(" (", limit = 2)

    val isSignatureValidOrWarning =
        status == ValidatorInterface.Status.Valid ||
            status == ValidatorInterface.Status.Warning ||
            status == ValidatorInterface.Status.NonQSCD

    val tagBackgroundColor = if (isSignatureValidOrWarning) Green_2_50 else Red50
    val tagContentColor = if (isSignatureValidOrWarning) Green_2_700 else Red800
    val additionalTextColor = if (status == ValidatorInterface.Status.Valid) Red800 else Yellow800

    FlowRow(
        modifier = modifier,
    ) {
        TagBadge(
            text = parts[0],
            backgroundColor = tagBackgroundColor,
            contentColor = tagContentColor,
            modifier =
                modifier
                    .alignByBaseline()
                    .focusable()
                    .testTag("signatureUpdateListSignatureStatus"),
        )

        if (parts.size > 1) {
            Text(
                text = " (${parts[1]}",
                color = additionalTextColor,
                modifier =
                    modifier
                        .alignByBaseline()
                        .focusable()
                        .testTag("signatureUpdateListSignatureStatusCaution"),
            )
        }
    }
}
