@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewMediumPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    title: String,
    contentDescription: String,
) {
    Row(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Checkbox(
                modifier =
                    modifier.semantics {
                        this.contentDescription = contentDescription
                    }
                        .padding(
                            top = screenViewLargePadding,
                            bottom = screenViewLargePadding,
                            end = screenViewMediumPadding,
                        ),
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
        Text(
            modifier = modifier.notAccessible(),
            text = title,
        )
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
