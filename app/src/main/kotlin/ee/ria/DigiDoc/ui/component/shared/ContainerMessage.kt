@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Yellow500

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContainerMessage(
    modifier: Modifier,
    text: String,
    testTag: String = "",
    color: Color = Yellow500,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = itemSpacingPadding)
                .semantics {
                    testTagsAsResourceId = true
                },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth(),
            color = color,
        ) {
            Text(
                modifier =
                    modifier
                        .padding(itemSpacingPadding)
                        .testTag(testTag),
                text = text,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.background,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
