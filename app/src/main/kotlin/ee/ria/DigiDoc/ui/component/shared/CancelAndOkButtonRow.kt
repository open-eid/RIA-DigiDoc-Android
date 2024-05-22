@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemEndPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun CancelAndOkButtonRow(
    modifier: Modifier = Modifier,
    cancelButtonEnabled: Boolean = true,
    okButtonEnabled: Boolean = true,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
    cancelButtonTitle: String,
    okButtonTitle: String,
    cancelButtonContentDescription: String,
    okButtonContentDescription: String,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = settingsItemEndPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Button(
            enabled = cancelButtonEnabled,
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
                            this.contentDescription = cancelButtonContentDescription
                        },
                textAlign = TextAlign.Center,
                text = cancelButtonTitle,
            )
        }
        Button(
            enabled = okButtonEnabled,
            modifier = modifier.weight(1f),
            shape = RectangleShape,
            colors =
                ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.tertiary,
                ),
            onClick = okButtonClick,
        ) {
            Text(
                modifier =
                    modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .semantics {
                            this.contentDescription = okButtonContentDescription
                        },
                textAlign = TextAlign.Center,
                text = okButtonTitle,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CancelAndOkButtonRowPreview() {
    RIADigiDocTheme {
        CancelAndOkButtonRow(
            cancelButtonTitle = stringResource(id = R.string.cancel_button),
            okButtonTitle = stringResource(id = R.string.sign_button),
            cancelButtonContentDescription = "",
            okButtonContentDescription = "",
        )
    }
}
