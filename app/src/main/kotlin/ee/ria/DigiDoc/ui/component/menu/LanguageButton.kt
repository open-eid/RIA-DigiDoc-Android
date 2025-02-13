@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import ee.ria.DigiDoc.ui.theme.BlueBackground
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.OnPrimary
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LanguageButton(
    modifier: Modifier = Modifier,
    onClickItem: () -> Unit = {},
    @StringRes label: Int,
    contentDescription: String,
    testTag: String = "",
) {
    val titleText = stringResource(id = label)
    Button(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = XSPadding)
                .wrapContentHeight()
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag(testTag),
        shape = RectangleShape,
        onClick = onClickItem,
        colors =
            ButtonColors(
                containerColor = Color.Transparent,
                contentColor = OnPrimary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        contentPadding =
            PaddingValues(
                zeroPadding,
            ),
    ) {
        ConstraintLayout(
            modifier =
                modifier
                    .wrapContentHeight()
                    .align(Alignment.CenterVertically),
        ) {
            val (
                languageButtonText,
                languageButtonArrow,
            ) = createRefs()

            Text(
                text = titleText,
                modifier =
                    modifier
                        .padding(start = SPadding, end = XSPadding)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .semantics {
                            this.contentDescription = contentDescription
                        }
                        .constrainAs(languageButtonText) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start,
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_arrow_forward_48dp_wght400),
                contentDescription = null,
                modifier =
                    modifier
                        .padding(XSPadding)
                        .size(iconSizeXXS)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .constrainAs(languageButtonArrow) {
                            start.linkTo(languageButtonText.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsItemPreview() {
    RIADigiDocTheme {
        Surface(color = BlueBackground) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LanguageButton(
                    label = R.string.main_home_menu_locale_et,
                    contentDescription =
                        stringResource(
                            id = R.string.main_home_menu_locale_et,
                        ).lowercase(),
                )
                LanguageButton(
                    label = R.string.main_home_menu_locale_en,
                    contentDescription =
                        stringResource(
                            id = R.string.main_home_menu_locale_en,
                        ).lowercase(),
                )
            }
        }
    }
}
