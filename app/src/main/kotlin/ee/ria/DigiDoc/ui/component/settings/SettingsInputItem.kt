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
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemEndPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemStartPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SettingsInputItem(
    modifier: Modifier = Modifier,
    value: String,
    onClickItem: () -> Unit = {},
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
                Dimensions.zeroPadding,
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
            var endPadding = Dimensions.settingsIconSize
            Column(
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
            ) {
                Text(
                    text = title,
                    modifier = modifier.wrapContentHeight(align = Alignment.CenterVertically),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = modifier.wrapContentHeight(align = Alignment.CenterVertically),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start,
                )
            }
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_edit),
                contentDescription = null,
                modifier =
                    modifier
                        .padding(start = settingsItemStartPadding)
                        .size(Dimensions.settingsIconSize)
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
fun SettingsInputItemPreview() {
    RIADigiDocTheme {
        Column {
            SettingsInputItem(
                value = "00000000-0000-0000-0000-000000000000",
                title = stringResource(id = R.string.main_settings_uuid_title),
                contentDescription = stringResource(id = R.string.main_settings_uuid_title).lowercase(),
            )
            SettingsInputItem(
                value = "https://eid-dd.ria.ee/ts",
                title = stringResource(id = R.string.main_settings_tsa_url_title),
                contentDescription = stringResource(id = R.string.main_settings_tsa_url_title).lowercase(),
            )
        }
    }
}
