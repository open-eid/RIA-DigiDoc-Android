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
import androidx.compose.material3.MaterialTheme
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
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.containerButtonHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding
import ee.ria.DigiDoc.ui.theme.Red500

@Composable
fun ContainerFile(
    modifier: Modifier = Modifier,
    dataFile: DataFileInterface,
    onClickView: () -> Unit = {},
    onClickRemove: () -> Unit = {},
    onClickSave: () -> Unit = {},
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
            onClick = onClickView,
            modifier =
                modifier
                    .padding(start = containerButtonHorizontalPadding, end = screenViewVerticalPadding)
                    .align(Alignment.CenterVertically)
                    .weight(1f)
                    .semantics {
                        contentDescription = "${fileText.lowercase()} ${dataFile.fileName.lowercase()}"
                    },
        ) {
            Text(
                text = dataFile.fileName,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleMedium,
                modifier = modifier.fillMaxWidth(),
            )
        }

        IconButton(
            onClick = onClickRemove,
            modifier = modifier.size(iconSize),
            content = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                    contentDescription = "${stringResource(id = R.string.document_remove_button)} ${dataFile.fileName}",
                    tint = Red500,
                )
            },
        )
        Spacer(modifier = modifier.width(itemSpacingPadding))
        IconButton(
            onClick = onClickSave,
            modifier = modifier.size(iconSize),
            content = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_save),
                    contentDescription = "${stringResource(id = R.string.document_save_button)} ${dataFile.fileName}",
                    tint = Blue500,
                )
            },
        )
    }
}
