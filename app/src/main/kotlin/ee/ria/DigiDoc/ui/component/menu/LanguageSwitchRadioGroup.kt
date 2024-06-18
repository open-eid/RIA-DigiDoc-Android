@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ee.ria.DigiDoc.ui.component.shared.RadioButton
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Language
import ee.ria.DigiDoc.viewmodel.SettingsViewModel
import java.util.Locale

@Composable
fun LanguageSwitchRadioGroup(
    modifier: Modifier = Modifier,
    selectedRadioItem: String,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val activity = (LocalContext.current as Activity)
    var selectedItem by remember { mutableStateOf(selectedRadioItem) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier =
            modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(
                    horizontal = screenViewLargePadding,
                    vertical = screenViewExtraLargePadding,
                ),
    ) {
        LanguageSwitchRadioItem().radioItems().forEachIndexed { _, languageItem ->
            RadioButton(
                modifier =
                    modifier
                        .fillMaxWidth(),
                selected = languageItem.locale == selectedItem,
                label = languageItem.label,
                contentDescription = languageItem.contentDescription,
                onClick = {
                    selectedItem = languageItem.locale
                    val locale = Locale(languageItem.locale)
                    settingsViewModel.dataStore.setLocale(locale)
                    activity.finish()
                    activity.startActivity(activity.intent)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureAddRadioGroupPreview() {
    RIADigiDocTheme {
        val selected by remember { mutableStateOf(Language.Estonian.locale) }
        LanguageSwitchRadioGroup(
            selectedRadioItem = selected,
        )
    }
}
