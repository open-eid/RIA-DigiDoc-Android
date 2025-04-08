@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.theme.Green_2_50
import ee.ria.DigiDoc.ui.theme.Green_2_700

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColoredRecipientStatusText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val tagBackgroundColor = Green_2_50
    val tagContentColor = Green_2_700

    FlowRow(
        modifier = modifier,
    ) {
        TagBadge(
            text = text,
            backgroundColor = tagBackgroundColor,
            contentColor = tagContentColor,
            modifier =
                modifier
                    .alignByBaseline()
                    .focusable()
                    .testTag("recipientListDecryptionStatus"),
        )
    }
}
