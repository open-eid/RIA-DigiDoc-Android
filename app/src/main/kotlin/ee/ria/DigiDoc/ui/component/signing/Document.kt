@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.MiddleEllipsizeMultilineText
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500

@Composable
fun Document(
    modifier: Modifier = Modifier,
    name: String,
    onItemClick: () -> Unit = {},
    onRemoveButtonClick: () -> Unit = {},
) {
    val documentTitle = stringResource(id = R.string.document)
    val textColor = MaterialTheme.colorScheme.inverseSurface.toArgb()
    ConstraintLayout(
        modifier =
            modifier
                .padding(vertical = screenViewSmallPadding)
                .wrapContentHeight()
                .fillMaxWidth()
                .semantics {
                    this.contentDescription = "$documentTitle ${name.lowercase()}"
                }
                .focusable(true)
                .clickable(onClick = onItemClick),
    ) {
        val (
            documentText,
            removeIcon,
        ) = createRefs()
        MiddleEllipsizeMultilineText(
            modifier =
                modifier
                    .wrapContentSize()
                    .padding(start = screenViewLargePadding, end = iconSize)
                    .padding(end = screenViewLargePadding)
                    .padding(end = screenViewLargePadding)
                    .constrainAs(documentText) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .focusable(false),
            text = name,
            maxLines = 4,
            textColor = textColor,
        )
        IconButton(
            modifier =
                modifier
                    .padding(end = screenViewLargePadding)
                    .size(iconSize)
                    .constrainAs(removeIcon) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
            onClick = onRemoveButtonClick,
            content = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                    contentDescription = "${
                        stringResource(
                            id = R.string.recent_documents_remove_button,
                        )
                    } $name",
                    tint = Red500,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DocumentPreview() {
    RIADigiDocTheme {
        Surface(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column {
                Document(
                    name = "test-container.asice",
                )
                Document(
                    name = "some-" + "very-".repeat(30) + "long-document-name-document.bdoc",
                )
                Document(
                    name = "some-" + "very-".repeat(40) + "long-document-name-document.ddoc",
                )
            }
        }
    }
}
