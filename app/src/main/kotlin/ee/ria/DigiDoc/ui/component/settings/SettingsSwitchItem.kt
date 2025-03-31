@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsSwitchItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    title: String,
    contentDescription: String,
    testTag: String = "",
) {
    Row(
        modifier =
            modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(vertical = MSPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier =
                modifier
                    .weight(1f)
                    .notAccessible(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            modifier =
                modifier
                    .padding(horizontal = XSPadding)
                    .semantics {
                        testTagsAsResourceId = true
                        this.contentDescription = contentDescription
                    }
                    .testTag(testTag),
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsRightsItemPreview() {
    RIADigiDocTheme {
        Column {
            SettingsSwitchItem(
                checked = false,
                title = stringResource(id = R.string.main_settings_open_all_filetypes_title),
                contentDescription = stringResource(id = R.string.main_settings_open_all_filetypes_title).lowercase(),
            )
            SettingsSwitchItem(
                checked = true,
                title = stringResource(id = R.string.main_settings_allow_screenshots_title),
                contentDescription = stringResource(id = R.string.main_settings_allow_screenshots_title).lowercase(),
            )
        }
    }
}
