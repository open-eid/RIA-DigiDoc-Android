@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonCornerShapeSize
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    contentDescription: String? = null,
    onClickItem: () -> Unit = {},
) {
    val titleText = stringResource(id = title)
    Button(
        modifier =
            modifier
                .fillMaxWidth(),
        shape = RoundedCornerShape(buttonCornerShapeSize),
        colors =
            ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.background,
            ),
        onClick = onClickItem,
    ) {
        Text(
            modifier =
                modifier
                    .wrapContentSize()
                    .semantics {
                        this.contentDescription =
                            contentDescription ?: titleText
                    },
            textAlign = TextAlign.Center,
            text = titleText,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonPreview() {
    RIADigiDocTheme {
        PrimaryButton(title = R.string.signature_home_create_button)
    }
}
