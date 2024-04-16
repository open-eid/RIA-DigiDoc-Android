@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.textVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SmartIdView(
    modifier: Modifier = Modifier,
    cancelButtonClick: () -> Unit = {},
) {
    Column {
        Text(
            text = stringResource(id = R.string.signature_update_smart_id_message),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.signature_update_smart_id_country),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
        )
        val countriesList = stringArrayResource(id = R.array.smart_id_country)
        CountrySpinner(
            list = countriesList,
            preselected = countriesList.first(),
            onSelectionChanged = {},
            modifier = modifier.fillMaxWidth().height(Dimensions.textFieldHeight),
        )
        Text(
            text = stringResource(id = R.string.signature_update_mobile_id_personal_code),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
        )
        var personalCodeText by remember { mutableStateOf(TextFieldValue(text = "")) }
        TextField(
            modifier = modifier.fillMaxWidth().height(Dimensions.textFieldHeight),
            value = personalCodeText,
            shape = RectangleShape,
            onValueChange = {
                personalCodeText = it
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        val rememberMeCheckedState = remember { mutableStateOf(true) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = rememberMeCheckedState.value,
                onCheckedChange = { rememberMeCheckedState.value = it },
            )
            Text(text = stringResource(id = R.string.signature_update_mobile_id_remember_me))
        }
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                modifier = modifier.weight(1f),
                shape = RectangleShape,
                colors =
                    ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.tertiary,
                    ),
                onClick = cancelButtonClick,
            ) {
                Text(
                    modifier =
                        modifier
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .semantics {
                                this.contentDescription = contentDescription
                            },
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.cancel_button),
                )
            }
            Button(
                modifier = modifier.weight(1f),
                shape = RectangleShape,
                enabled = false,
                colors =
                    ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.tertiary,
                    ),
                onClick = { /* TODO */ },
            ) {
                Text(
                    modifier =
                        modifier
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .semantics {
                                this.contentDescription = contentDescription
                            },
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.sign_button),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartIdViewPreview() {
    RIADigiDocTheme {
        SmartIdView()
    }
}
