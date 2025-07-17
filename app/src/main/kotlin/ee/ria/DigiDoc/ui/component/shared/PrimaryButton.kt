@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonCornerShapeSize
import ee.ria.DigiDoc.ui.theme.Dimensions.noBorderStroke
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Transparent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    contentDescription: String? = null,
    isSubButton: Boolean = false,
    isFocusable: Boolean = true,
    containerColor: Color =
        if (isSubButton) {
            MaterialTheme.colorScheme.background
        } else {
            Color.Transparent
        },
    contentColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClickItem: () -> Unit = {},
) {
    val titleText = stringResource(id = title)
    Button(
        modifier =
            modifier
                .semantics(mergeDescendants = true) {
                    testTagsAsResourceId = true
                }.fillMaxWidth()
                .focusable(enabled = isFocusable),
        shape = RoundedCornerShape(buttonCornerShapeSize),
        contentPadding = PaddingValues(zeroPadding),
        enabled = enabled,
        colors =
            ButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledContentColor = MaterialTheme.colorScheme.tertiary,
            ),
        border =
            if (isSubButton) {
                BorderStroke(
                    width = SBorder,
                    color = contentColor,
                )
            } else if (!enabled) {
                BorderStroke(
                    width = SBorder,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            } else {
                BorderStroke(noBorderStroke, Transparent)
            },
        onClick = onClickItem,
    ) {
        Text(
            modifier =
                Modifier
                    .wrapContentSize()
                    .semantics {
                        this.contentDescription =
                            contentDescription ?: titleText
                    },
            textAlign = TextAlign.Center,
            text = titleText,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonPreview() {
    RIADigiDocTheme {
        Column {
            PrimaryButton(title = R.string.signature_home_create_button)
            PrimaryButton(title = R.string.signature_home_create_button, isSubButton = true)
        }
    }
}
