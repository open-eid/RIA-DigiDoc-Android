@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.shared.TextRadioButton
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSivaCategoryDialog(
    modifier: Modifier = Modifier,
    sivaSettingSelected: String = SivaSetting.DEFAULT.name,
    issuedTo: String = "",
    validTo: String = "",
    settingsSivaServiceUrl: TextFieldValue = TextFieldValue(""),
    defaultSivaServiceUrl: String = "",
    onClickBack: () -> Unit = {},
    onClickSivaSettingDefault: () -> Unit = {},
    onClickSivaSettingManual: () -> Unit = {},
    onAddCertificateClick: () -> Unit = {},
    onShowCertificateClick: () -> Unit = {},
    onSettingsSivaUrlValueChanged: (TextFieldValue) -> Unit = {},
) {
    val sivaDialogTitle = stringResource(id = R.string.main_settings_siva_service_title)

    Column(
        modifier =
            modifier
                .padding(itemSpacingPadding)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("mainSettingsSivaServiceContainer"),
    ) {
        BackButton(
            modifier =
                modifier
                    .semantics { traversalIndex = 1f }
                    .testTag("mainSettingsSivaBackButton"),
            onClickBack = onClickBack,
        )
        Text(
            modifier =
                modifier
                    .padding(horizontal = screenViewLargePadding, vertical = screenViewLargePadding)
                    .fillMaxWidth()
                    .clearAndSetSemantics {
                        traversalIndex = 0f
                        heading()
                        this.contentDescription = sivaDialogTitle.lowercase()
                    }
                    .focusProperties { canFocus = true }
                    .focusTarget()
                    .focusable(),
            text = sivaDialogTitle,
            style = MaterialTheme.typography.titleLarge,
        )
        Column(
            modifier = modifier.testTag("mainSettingsSivaServiceChoiceGroup"),
        ) {
            TextRadioButton(
                modifier =
                    modifier
                        .semantics { traversalIndex = 2f }
                        .testTag("mainSettingsSivaServiceDefaultChoice"),
                title = stringResource(id = R.string.main_settings_siva_default_access_title),
                contentDescription =
                    "${sivaDialogTitle.lowercase()} " +
                        stringResource(id = R.string.main_settings_siva_default_access_title).lowercase(),
                selected = sivaSettingSelected == SivaSetting.DEFAULT.name,
                onClick = onClickSivaSettingDefault,
            )
            TextRadioButton(
                modifier =
                    modifier
                        .semantics { traversalIndex = 3f }
                        .testTag("mainSettingsSivaServiceManualChoice"),
                title = stringResource(id = R.string.main_settings_siva_default_manual_access_title),
                contentDescription =
                    "${sivaDialogTitle.lowercase()} " +
                        stringResource(id = R.string.main_settings_siva_default_manual_access_title).lowercase(),
                selected = sivaSettingSelected == SivaSetting.MANUAL.name,
                onClick = onClickSivaSettingManual,
            )
        }
        val defaultLabel = stringResource(id = R.string.main_settings_siva_service_url)
        val defaultPlaceHolder =
            defaultSivaServiceUrl.ifEmpty {
                stringResource(id = R.string.main_settings_siva_service_url)
            }
        val label = remember { mutableStateOf(defaultPlaceHolder) }
        val placeholder = remember { mutableStateOf(defaultPlaceHolder) }

        TextField(
            enabled = sivaSettingSelected == SivaSetting.MANUAL.name,
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .fillMaxWidth()
                    .semantics { traversalIndex = 4f }
                    .testTag("mainSettingsSivaServiceUrl")
                    .onFocusChanged {
                        label.value =
                            if (it.isFocused) {
                                defaultLabel
                            } else {
                                if (settingsSivaServiceUrl.text.isEmpty()) {
                                    defaultPlaceHolder
                                } else {
                                    defaultLabel
                                }
                            }
                    },
            value = settingsSivaServiceUrl,
            shape = RectangleShape,
            onValueChange =
                {
                    placeholder.value =
                        if (it.text.isEmpty()) {
                            defaultPlaceHolder
                        } else {
                            ""
                        }
                    onSettingsSivaUrlValueChanged(it)
                },
            label = {
                Text(text = label.value)
            },
            placeholder = {
                Text(text = placeholder.value)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )

        if (sivaSettingSelected == SivaSetting.MANUAL.name) {
            Text(
                modifier =
                    modifier
                        .padding(horizontal = screenViewLargePadding, vertical = screenViewLargePadding)
                        .fillMaxWidth()
                        .semantics { traversalIndex = 5f },
                text = stringResource(id = R.string.main_settings_siva_certificate_title),
            )
            Text(
                modifier =
                    modifier
                        .padding(horizontal = screenViewLargePadding, vertical = screenViewLargePadding)
                        .fillMaxWidth()
                        .semantics { traversalIndex = 6f }
                        .testTag("mainSettingsSivaCertificateIssuedTo"),
                text = stringResource(id = R.string.main_settings_timestamp_cert_issued_to_title) + " " + issuedTo,
            )
            val parts = validTo.split(" (", limit = 2)
            val contentColor = Red500
            FlowRow(
                modifier =
                    modifier
                        .padding(
                            horizontal = screenViewLargePadding,
                            vertical = screenViewLargePadding,
                        ),
            ) {
                Text(
                    modifier =
                        modifier
                            .semantics { traversalIndex = 7f }
                            .testTag("mainSettingsSivaCertificateValidTo"),
                    text = stringResource(id = R.string.main_settings_timestamp_cert_valid_to_title) + " " + parts[0],
                )
                if (parts.size > 1 && parts[1].isNotEmpty()) {
                    Text(
                        text = " (${parts[1]}",
                        color = contentColor,
                        modifier =
                            modifier
                                .focusable(false)
                                .semantics { traversalIndex = 10f }
                                .testTag("mainSettingsSivaCertificateValidToWarning"),
                    )
                }
            }
            PrimaryButton(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            horizontal = screenViewLargePadding,
                        )
                        .semantics { traversalIndex = 8f }
                        .testTag("mainSettingsSivaCertificateAddCertificateButton"),
                contentDescription =
                    stringResource(
                        id = R.string.main_settings_timestamp_cert_add_certificate_button,
                    ).lowercase(),
                title = R.string.main_settings_timestamp_cert_add_certificate_button,
                onClickItem = onAddCertificateClick,
            )
            PrimaryButton(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            horizontal = screenViewLargePadding,
                        )
                        .semantics { traversalIndex = 9f }
                        .testTag("mainSettingsSivaCertificateShowCertificateButton"),
                contentDescription =
                    stringResource(
                        id = R.string.main_settings_timestamp_cert_show_certificate_button,
                    ).lowercase(),
                title = R.string.main_settings_timestamp_cert_show_certificate_button,
                onClickItem = onShowCertificateClick,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsSivaCategoryDialogPreview() {
    RIADigiDocTheme {
        SettingsSivaCategoryDialog()
    }
}
