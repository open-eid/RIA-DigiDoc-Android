@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.containerButtonHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding

@Composable
fun ContainerFile(
    modifier: Modifier = Modifier,
    fileName: String,
) {
    val fileText = stringResource(id = R.string.file)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(end = screenViewHorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = { /* TODO */ },
            modifier =
                modifier
                    .padding(start = containerButtonHorizontalPadding, end = screenViewVerticalPadding)
                    .align(Alignment.CenterVertically)
                    .weight(1f)
                    .semantics {
                        contentDescription = "${fileText.lowercase()} ${fileName.lowercase()}"
                    },
        ) {
            Text(
                text = fileName,
                textAlign = TextAlign.Start,
                modifier = modifier.fillMaxWidth(),
            )
        }

        IconButton(
            onClick = { /* TODO */ },
            modifier = modifier.size(Dimensions.iconSize),
            content = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                    contentDescription = "${stringResource(id = R.string.document_remove_button)} $fileName",
                )
            },
        )
        Spacer(modifier = modifier.width(itemSpacingPadding))
        IconButton(
            onClick = { /* TODO */ },
            modifier = modifier.size(Dimensions.iconSize),
            content = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_save),
                    contentDescription = "${stringResource(id = R.string.document_save_button)} $fileName",
                )
            },
        )
    }
}
