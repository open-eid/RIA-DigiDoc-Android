@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.theme.Green800
import ee.ria.DigiDoc.ui.theme.Red800
import ee.ria.DigiDoc.ui.theme.Yellow800

@Composable
fun ColoredSignedStatusText(
    text: String,
    status: ValidatorInterface.Status,
    modifier: Modifier,
) {
    val parts = text.split(" (", limit = 2)

    Row {
        Text(
            text = parts[0],
            color =
                if (status == ValidatorInterface.Status.Valid ||
                    status == ValidatorInterface.Status.Warning ||
                    status == ValidatorInterface.Status.NonQSCD
                ) {
                    Green800
                } else {
                    Red800
                },
            modifier = modifier.focusable(),
        )

        if (parts.size > 1) {
            Text(
                text = " (${parts[1]}",
                color =
                    if (status != ValidatorInterface.Status.Valid) {
                        Yellow800
                    } else {
                        Red800
                    },
                modifier = modifier.focusable(),
            )
        }
    }
}
