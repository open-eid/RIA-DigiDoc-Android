@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeLarge
import ee.ria.DigiDoc.ui.theme.Dimensions.toolbarHeight
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuToolbarScreen(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit = {},
    title: String,
) {
    ConstraintLayout(
        modifier =
            modifier
                .height(toolbarHeight)
                .fillMaxWidth()
                .semantics {
                    testTagsAsResourceId = true
                },
    ) {
        val (
            titleText,
            menuButton,
        ) = createRefs()
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier =
                modifier
                    .height(toolbarHeight)
                    .padding(start = iconSizeLarge)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .constrainAs(titleText) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(menuButton.start)
                        bottom.linkTo(parent.bottom)
                    },
        )
        IconButton(
            modifier =
                modifier
                    .size(iconSizeLarge)
                    .constrainAs(menuButton) {
                        end.linkTo(parent.end)
                    }
                    .testTag("mainHomeMenuCloseButton"),
            onClick = onClickBack,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_clear),
                contentDescription = stringResource(id = R.string.close_menu),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ToolbarScreenPreview() {
    RIADigiDocTheme {
        MenuToolbarScreen(
            title = "",
        )
    }
}
