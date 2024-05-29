@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeLarge
import ee.ria.DigiDoc.ui.theme.Dimensions.toolbarHeight
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun HomeToolbar(
    modifier: Modifier = Modifier,
    onClickMenu: () -> Unit = {},
) {
    ConstraintLayout(
        modifier = modifier.height(toolbarHeight).fillMaxWidth(),
    ) {
        val (
            digiDocIcon,
            menuButton,
        ) = createRefs()
        Image(
            modifier =
                Modifier
                    .height(toolbarHeight)
                    .padding(start = iconSizeLarge)
                    .constrainAs(digiDocIcon) {
                        start.linkTo(parent.start)
                        end.linkTo(menuButton.start)
                    },
            imageVector = ImageVector.vectorResource(id = R.drawable.main_home_toolbar_logo),
            contentDescription = stringResource(id = R.string.main_home_logo),
        )
        IconButton(
            modifier =
                Modifier
                    .size(iconSizeLarge)
                    .constrainAs(menuButton) {
                        end.linkTo(parent.end)
                    },
            onClick = onClickMenu,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_more_vert),
                contentDescription = stringResource(id = R.string.main_home_menu_button),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeToolbarPreview() {
    RIADigiDocTheme {
        HomeToolbar()
    }
}
