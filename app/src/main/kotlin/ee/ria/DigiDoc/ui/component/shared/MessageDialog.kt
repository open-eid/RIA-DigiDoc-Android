@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun MessageDialog(
    modifier: Modifier = Modifier,
    title: String,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
    cancelButtonContentDescription: String = stringResource(id = R.string.cancel_button).lowercase(),
    okButtonContentDescription: String = stringResource(id = R.string.ok_button).lowercase(),
) {
    Column(
        modifier = modifier.padding(screenViewLargePadding),
    ) {
        DynamicText(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = screenViewLargePadding),
            text = title,
            textStyle = MaterialTheme.typography.bodyLarge,
        )
        CancelAndOkButtonRow(
            cancelButtonClick = cancelButtonClick,
            okButtonClick = okButtonClick,
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.ok_button,
            cancelButtonContentDescription = cancelButtonContentDescription,
            okButtonContentDescription = okButtonContentDescription,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessageDialogPreview() {
    RIADigiDocTheme {
        MessageDialog(
            title = "Container name",
        )
    }
}
