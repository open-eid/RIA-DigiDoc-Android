@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.view.accessibility.AccessibilityEvent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RadioButtonItem(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes changedLabel: Int,
    contentDescription: String,
    testTag: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val context = LocalContext.current
    val titleText = stringResource(id = title)
    val changedText = stringResource(id = changedLabel)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titleText,
            modifier = modifier.weight(1f),
        )

        RadioButton(
            modifier =
                Modifier
                    .semantics {
                        testTagsAsResourceId = true
                        this.contentDescription = contentDescription
                    }
                    .testTag(testTag),
            selected = isSelected,
            onClick = {
                onSelect()
                AccessibilityUtil.sendAccessibilityEvent(
                    context,
                    AccessibilityEvent.TYPE_ANNOUNCEMENT,
                    changedText,
                )
            },
        )
    }
}
