@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import ee.ria.DigiDoc.utilsLib.container.NameUtil

@Composable
fun StyledNameText(
    modifier: Modifier = Modifier,
    name: String,
) {
    val formattedName = NameUtil.formatName(name)

    val nameParts = formattedName.split(", ").map { it.trim() }

    val styledText =
        buildAnnotatedString {
            if (nameParts.size == 2) {
                val (fullName, code) = nameParts

                val nameSplit = fullName.split(" ")
                val firstName = nameSplit.getOrNull(0) ?: ""
                val lastName = nameSplit.getOrNull(1) ?: ""

                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append("$firstName ")
                pop()

                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(lastName)
                pop()

                append(", $code")
            } else {
                append(formattedName)
            }
        }

    Text(
        modifier = modifier,
        text = styledText,
    )
}
