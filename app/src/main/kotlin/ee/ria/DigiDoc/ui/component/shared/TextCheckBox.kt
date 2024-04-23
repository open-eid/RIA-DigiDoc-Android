@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun TextCheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    title: String,
    contentDescription: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            modifier =
                modifier.semantics {
                    this.contentDescription = contentDescription
                },
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Text(text = title)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TextCheckBoxPreview() {
    RIADigiDocTheme {
        Column {
            TextCheckBox(checked = true, title = "Option 1".repeat(10), contentDescription = "")
            TextCheckBox(checked = false, title = "Option 2", contentDescription = "")
            TextCheckBox(checked = true, title = "Option 3", contentDescription = "")
            TextCheckBox(checked = false, title = "Option 4", contentDescription = "")
        }
    }
}
