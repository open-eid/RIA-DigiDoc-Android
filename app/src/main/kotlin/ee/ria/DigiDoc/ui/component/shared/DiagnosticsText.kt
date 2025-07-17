@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding

@Composable
fun DiagnosticsText(
    modifier: Modifier,
    text1: String,
    text2: String,
    textAlign: TextAlign = TextAlign.Start,
) {
    val annotatedString =
        buildAnnotatedString {
            append(text1)
            append(text2)
        }

    Text(
        modifier =
            modifier
                .semantics {
                    contentDescription = text1.lowercase() + " " + text2.lowercase()
                }.padding(
                    start = screenViewLargePadding,
                    top = screenViewSmallPadding,
                    end = screenViewLargePadding,
                ),
        text = annotatedString,
        textAlign = textAlign,
        style = MaterialTheme.typography.bodyLarge,
    )
}
