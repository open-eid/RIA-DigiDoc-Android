@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeLarge
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@Composable
fun SettingsSwitchItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    title: String,
    contentDescription: String,
    testTag: String = "",
) {
    ConstraintLayout(
        modifier =
            modifier
                .padding(vertical = screenViewLargePadding)
                .wrapContentHeight()
                .fillMaxWidth(),
    ) {
        val (
            settingsRightsItemText,
            settingsRightsItemCheck,
        ) = createRefs()
        Text(
            text = title,
            modifier =
                modifier
                    .notAccessible()
                    .wrapContentSize()
                    .padding(start = screenViewLargePadding, end = iconSizeLarge)
                    .padding(end = screenViewLargePadding)
                    .padding(end = screenViewLargePadding)
                    .constrainAs(settingsRightsItemText) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier =
                modifier
                    .semantics { this.contentDescription = contentDescription }
                    .padding(end = screenViewLargePadding)
                    .size(iconSizeLarge)
                    .constrainAs(settingsRightsItemCheck) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .testTag(testTag),
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
