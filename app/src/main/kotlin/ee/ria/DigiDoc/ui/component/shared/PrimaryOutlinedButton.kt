@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PrimaryOutlinedButton(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @DrawableRes iconRes: Int = 0,
    contentDescription: String? = null,
    isFocusable: Boolean = true,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClickItem: () -> Unit = {},
) {
    val titleText = stringResource(id = title)
    OutlinedButton(
        modifier =
            modifier
                .semantics(mergeDescendants = true) {
                    testTagsAsResourceId = true
                }
                .fillMaxWidth()
                .padding(
                    horizontal = XSPadding,
                )
                .focusable(enabled = isFocusable),
        shape = buttonRoundCornerShape,
        contentPadding = PaddingValues(zeroPadding),
        enabled = enabled,
        colors =
            ButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledContentColor = MaterialTheme.colorScheme.tertiary,
            ),
        onClick = onClickItem,
    ) {
        if (iconRes != 0) {
            Icon(
                modifier = modifier.size(iconSizeXXS),
                imageVector =
                    ImageVector.vectorResource(
                        id = iconRes,
                    ),
                contentDescription = null,
            )
            Spacer(modifier = modifier.width(XSPadding))
        }
        Text(
            modifier =
                modifier
                    .semantics {
                        this.contentDescription =
                            contentDescription ?: titleText
                    },
            text = titleText,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryOutlinedButtonPreview() {
    RIADigiDocTheme {
        Column {
            PrimaryOutlinedButton(title = R.string.signature_home_create_button)
            PrimaryOutlinedButton(
                title = R.string.signature_home_create_button,
                iconRes = R.drawable.ic_m3_download_48dp_wght400,
            )
        }
    }
}
