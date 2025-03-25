@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings.advanced.signingservices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.settings.UUIDSetting
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MobileIdAndSmartIdServicesComponent(
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    val context = LocalContext.current

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val getSettingsUUID = sharedSettingsViewModel.dataStore::getSettingsUUID
    val getUuidSetting = sharedSettingsViewModel.dataStore::getUuidSetting
    val setSettingsUuid = sharedSettingsViewModel.dataStore::setSettingsUUID
    val setUuidSetting = sharedSettingsViewModel.dataStore::setUuidSetting
    val defaultUuid = getSettingsUUID()
    var settingsUuidChoice = remember { mutableStateOf(getUuidSetting().name) }
    var settingsUuid by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = defaultUuid,
                selection = TextRange(defaultUuid.length),
            ),
        )
    }
    sharedSettingsViewModel.updateTsaData(settingsUuid.text, context)

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SPadding)
                .padding(top = SPadding),
    ) {
        Text(
            text = stringResource(R.string.main_settings_uuid_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(bottom = SPadding),
        )

        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = XSPadding, bottom = SPadding),
            shape = buttonRoundedCornerShape,
            border =
                BorderStroke(
                    width = XSBorder,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(SPadding)
                        .clickable {
                            settingsUuidChoice.value = UUIDSetting.DEFAULT.name
                            setUuidSetting(UUIDSetting.DEFAULT)
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.main_settings_siva_default_access_title),
                    modifier = modifier.weight(1f),
                )
                RadioButton(
                    selected = settingsUuidChoice.value == UUIDSetting.DEFAULT.name,
                    onClick = {
                        settingsUuidChoice.value = UUIDSetting.DEFAULT.name
                        setUuidSetting(UUIDSetting.DEFAULT)
                    },
                )
            }
        }

        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = XSPadding, bottom = SPadding)
                    .clickable {
                        settingsUuidChoice.value = UUIDSetting.MANUAL.name
                        setUuidSetting(UUIDSetting.MANUAL)
                    },
            shape = buttonRoundedCornerShape,
            border =
                BorderStroke(
                    width = XSBorder,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column(
                modifier = modifier.padding(SPadding),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.main_settings_siva_default_manual_access_title),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = modifier.weight(1f),
                    )
                    RadioButton(
                        selected = settingsUuidChoice.value == UUIDSetting.MANUAL.name,
                        onClick = {
                            settingsUuidChoice.value = UUIDSetting.MANUAL.name
                            setUuidSetting(UUIDSetting.MANUAL)
                        },
                    )
                }

                if (settingsUuidChoice.value == UUIDSetting.MANUAL.name) {
                    Spacer(modifier = modifier.height(XSPadding))

                    OutlinedTextField(
                        enabled = settingsUuidChoice.value == UUIDSetting.MANUAL.name,
                        value = settingsUuid,
                        singleLine = true,
                        onValueChange = {
                            settingsUuid = it
                            setSettingsUuid(it.text)
                        },
                        shape = RectangleShape,
                        label = { Text(stringResource(R.string.main_settings_uuid_title)) },
                        modifier = modifier.fillMaxWidth(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) {
                                    ImageVector.vectorResource(id = R.drawable.ic_visibility)
                                } else {
                                    ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                                }
                            val description =
                                if (passwordVisible) {
                                    stringResource(
                                        id = R.string.hide_password,
                                    )
                                } else {
                                    stringResource(id = R.string.show_password)
                                }
                            IconButton(
                                modifier =
                                    modifier
                                        .semantics { traversalIndex = 9f }
                                        .testTag("mainSettingsUUIDPasswordVisibleButton"),
                                onClick = { passwordVisible = !passwordVisible },
                            ) {
                                Icon(imageVector = image, description)
                            }
                        },
                        textStyle = MaterialTheme.typography.titleSmall,
                        visualTransformation =
                            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Password,
                            ),
                    )
                }
            }
        }

        InvisibleElement(modifier = modifier)
    }
}
