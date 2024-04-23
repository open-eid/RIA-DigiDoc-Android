@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import ee.ria.DigiDoc.ui.theme.Dimensions.menuItemEndPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.menuItemHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.menuItemStartPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun MenuItem(
    modifier: Modifier = Modifier,
    onClickItem: () -> Unit = {},
    imageVector: ImageVector,
    title: String,
    contentDescription: String,
) {
    Button(
        modifier =
            modifier
                .padding(vertical = Dimensions.settingsItemEndPadding)
                .wrapContentHeight(Alignment.CenterVertically),
        shape = RectangleShape,
        colors =
            ButtonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        onClick = onClickItem,
    ) {
        ConstraintLayout(
            modifier =
                modifier.height(menuItemHeight)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
        ) {
            val (
                menuButtonHelpText,
                menuButtonHelpIcon,
            ) = createRefs()
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier =
                    modifier
                        .padding(start = menuItemStartPadding, end = menuItemEndPadding)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .constrainAs(menuButtonHelpIcon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
            )
            Text(
                modifier =
                    modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .semantics {
                            this.contentDescription = contentDescription
                        }
                        .constrainAs(menuButtonHelpText) {
                            start.linkTo(menuButtonHelpIcon.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start,
                text = title,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MenuItemPreview() {
    RIADigiDocTheme {
        MenuItem(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_help_outline),
            title = stringResource(id = R.string.main_home_menu_help),
            contentDescription = stringResource(id = R.string.main_home_menu_help_accessibility),
        )
    }
}
