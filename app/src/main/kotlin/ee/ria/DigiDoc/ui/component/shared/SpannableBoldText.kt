@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding

@Composable
fun SpannableBoldText(
    modifier: Modifier,
    boldText: String,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
) {
    val annotatedString =
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(boldText)
            }
            append(text)
        }

    Text(
        modifier =
            modifier.padding(
                start = screenViewLargePadding,
                top = screenViewSmallPadding,
                end = screenViewLargePadding,
            ),
        text = annotatedString,
        textAlign = textAlign,
        style = MaterialTheme.typography.bodyLarge,
    )
}
