@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.theme.Green50
import ee.ria.DigiDoc.ui.theme.Green800
import ee.ria.DigiDoc.ui.theme.Red50
import ee.ria.DigiDoc.ui.theme.Red800
import ee.ria.DigiDoc.ui.theme.Yellow800

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

    val tagBackgroundColor = if (isSignatureValidOrWarning) Green50 else Red50
    val tagContentColor = if (isSignatureValidOrWarning) Green800 else Red800
    val additionalTextColor = if (status == ValidatorInterface.Status.Valid) Red800 else Yellow800

    Column {
        TagBadge(
            text = parts[0],
            backgroundColor = tagBackgroundColor,
            contentColor = tagContentColor,
            modifier = modifier.focusable(),
        )

        if (parts.size > 1) {
            Text(
                text = " (${parts[1]}",
                color = additionalTextColor,
                modifier = modifier.focusable(),
            )
        }
    }
}
