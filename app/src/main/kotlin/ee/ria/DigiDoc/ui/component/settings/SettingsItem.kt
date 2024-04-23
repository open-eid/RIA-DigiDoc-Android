@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsIconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemEndPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemStartPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    onClickItem: () -> Unit = {},
    imageVector: ImageVector?,
    title: String,
    contentDescription: String,
) {
    Button(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = settingsItemEndPadding)
                .wrapContentHeight(),
        shape = RectangleShape,
        onClick = onClickItem,
        colors =
            ButtonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
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
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
        ) {
            val (
                settingsButtonText,
                settingsButtonIcon,
                settingsButtonArrow,
            ) = createRefs()
            var endPadding = settingsIconSize
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier =
                        modifier
                            .size(settingsIconSize)
                            .padding(horizontal = settingsItemStartPadding)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .constrainAs(settingsButtonIcon) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            },
                )
                endPadding = settingsIconSize * 2
            }

            Text(
                text = title,
                modifier =
                    modifier
                        .padding(start = settingsItemStartPadding, end = endPadding)
                        .padding(end = settingsItemStartPadding)
                        .padding(end = settingsItemStartPadding)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .semantics {
                            this.contentDescription = contentDescription
                        }
                        .constrainAs(settingsButtonText) {
                            start.linkTo(settingsButtonIcon.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start,
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_arrow_right),
                contentDescription = null,
                modifier =
                    modifier
                        .padding(start = settingsItemStartPadding)
                        .size(settingsIconSize)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .constrainAs(settingsButtonArrow) {
                            end.linkTo(parent.end)
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
        Column {
            SettingsItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_signing),
                title = stringResource(id = R.string.main_settings_siva_service_title),
                contentDescription = stringResource(id = R.string.main_settings_siva_service_title).lowercase(),
            )
            SettingsItem(
                imageVector = null,
                title = stringResource(id = R.string.main_settings_siva_service_title),
                contentDescription = stringResource(id = R.string.main_settings_siva_service_title).lowercase(),
            )
        }
    }
}
