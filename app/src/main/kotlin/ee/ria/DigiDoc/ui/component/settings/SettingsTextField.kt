@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@Composable
fun SettingsTextField(
    modifier: Modifier = Modifier,
    defaultValue: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit = {},
    useDefaultChecked: Boolean = true,
    useDefaultCheckedChange: (Boolean) -> Unit = {},
    title: String,
    contentDescription: String,
    testTag: String = "",
    isPasswordField: Boolean = false,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val keyboardType =
        if (isPasswordField) {
            KeyboardType.Password
        } else {
            KeyboardType.Ascii
        }
    Column(
        modifier =
            modifier
                .padding(start = screenViewLargePadding, end = screenViewLargePadding)
                .wrapContentHeight(align = Alignment.CenterVertically)
                .semantics {
                    this.contentDescription = contentDescription
                    isTraversalGroup = true
                }
                .testTag(testTag),
    ) {
        Text(
            text = title,
            modifier =
                modifier
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .notAccessible(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
        )
        TextField(
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .semantics {
                        this.contentDescription = contentDescription
                        traversalIndex = 1f
                    }
                    .fillMaxWidth(),
            shape = RectangleShape,
            enabled = !useDefaultChecked,
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = defaultValue)
            },
            maxLines = 1,
            singleLine = true,
            visualTransformation =
                if (!isPasswordField || passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = keyboardType,
                ),
            trailingIcon = {
                if (isPasswordField) {
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
                                .testTag("mainSettingsProxyPasswordVisibleButton"),
                        onClick = { passwordVisible = !passwordVisible },
                    ) {
                        Icon(imageVector = image, description)
                    }
                }
            },
        )
        TextCheckBox(
            modifier =
                modifier
                    .semantics { traversalIndex = 0f },
            checked = useDefaultChecked,
            onCheckedChange = useDefaultCheckedChange,
            title = stringResource(id = R.string.main_settings_tsa_url_use_default),
            contentDescription =
                "${stringResource(id = R.string.main_settings_tsa_url_use_default).lowercase()} " +
                    contentDescription,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsTextFieldPreview() {
    RIADigiDocTheme {
        Column {
            SettingsTextField(
                value = TextFieldValue("00000000-0000-0000-0000-000000000000"),
                defaultValue = "00000000-0000-0000-0000-000000000000",
                title = stringResource(id = R.string.main_settings_uuid_title),
                contentDescription = stringResource(id = R.string.main_settings_uuid_title).lowercase(),
            )
            SettingsTextField(
                value = TextFieldValue("https://eid-dd.ria.ee/ts"),
                defaultValue = "https://eid-dd.ria.ee/ts",
                title = stringResource(id = R.string.main_settings_tsa_url_title),
                contentDescription = stringResource(id = R.string.main_settings_tsa_url_title).lowercase(),
            )
        }
    }
}
