@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun MessageDialog(
    modifier: Modifier = Modifier,
    @StringRes title: Int = 0,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
    @StringRes leftButtonTitle: Int = R.string.cancel_button,
    @StringRes rightButtonTitle: Int = R.string.ok_button,
    @StringRes leftButtonContentDescription: Int = R.string.cancel_button,
    @StringRes rightButtonContentDescription: Int = R.string.ok_button,
) {
    Column(
        modifier = modifier.padding(SPadding),
    ) {
        DynamicText(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = SPadding),
            text = stringResource(id = title),
            textStyle =
                TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    textAlign = TextAlign.Start,
                ),
        )
        CancelAndOkButtonRow(
            cancelButtonClick = cancelButtonClick,
            okButtonClick = okButtonClick,
            cancelButtonTitle = leftButtonTitle,
            okButtonTitle = rightButtonTitle,
            cancelButtonContentDescription = stringResource(id = leftButtonContentDescription).lowercase(),
            okButtonContentDescription = stringResource(id = rightButtonContentDescription).lowercase(),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessageDialogPreview() {
    RIADigiDocTheme {
        MessageDialog(
            title = R.string.recent_documents_remove_confirmation_message,
        )
    }
}
