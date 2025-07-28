@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    @StringRes buttonStringRes: Int,
    @DrawableRes buttonIcon: Int,
    @StringRes buttonStringResContentDescription: Int,
    buttonClick: () -> Unit = {},
    testTag: String = "",
) {
    val contentDescription = stringResource(id = buttonStringResContentDescription)
    Button(
        onClick = buttonClick,
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag(testTag)
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(vertical = XSPadding),
        colors =
            ButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        contentPadding = PaddingValues(zeroPadding),
    ) {
        ConstraintLayout(
            modifier =
                modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
        ) {
            val (
                menuButtonIcon,
                menuButtonText,
                menuButtonArrow,
            ) = createRefs()
            Icon(
                modifier =
                    modifier
                        .constrainAs(menuButtonIcon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }.padding(horizontal = MSPadding)
                        .size(iconSizeXXS),
                imageVector = ImageVector.vectorResource(id = buttonIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier =
                    modifier
                        .padding(end = iconSizeXXS + iconSizeXXS + MSPadding * 3)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .semantics {
                            this.contentDescription = contentDescription
                        }.constrainAs(menuButtonText) {
                            start.linkTo(menuButtonIcon.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                text = stringResource(buttonStringRes),
            )
            Icon(
                modifier =
                    modifier
                        .constrainAs(menuButtonArrow) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }.padding(horizontal = MSPadding)
                        .size(iconSizeXXS),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_arrow_right_48dp_wght400),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MenuButtonPreview() {
    RIADigiDocTheme {
        MenuButton(
            buttonStringRes = R.string.main_home_menu_about,
            buttonIcon = R.drawable.ic_m3_info_48dp_wght400,
            buttonStringResContentDescription = R.string.main_home_menu_about_accessibility,
        )
    }
}
