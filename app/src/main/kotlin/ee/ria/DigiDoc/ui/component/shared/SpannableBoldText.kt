@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SpannableBoldText(
    modifier: Modifier,
    boldText: String,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
) {
    Column(
        modifier =
            modifier
                .semantics(mergeDescendants = true) {
                    testTagsAsResourceId = true
                }
                .focusGroup(),
    ) {
        Text(
            modifier =
                modifier
                    .focusable(false)
                    .padding(top = SPadding)
                    .padding(
                        horizontal = SPadding,
                    ),
            text = boldText,
            textAlign = textAlign,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (text != "") {
            Text(
                modifier =
                    modifier
                        .focusable(false)
                        .padding(
                            horizontal = SPadding,
                        ),
                text = text,
                textAlign = textAlign,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
