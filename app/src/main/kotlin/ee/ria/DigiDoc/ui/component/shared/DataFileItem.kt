@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewMediumPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun DataFileItem(
    modifier: Modifier = Modifier,
    dataFiles: List<DataFileInterface>,
    onClick: (DataFileInterface) -> Unit,
) {
    val fileDescription = stringResource(R.string.file)
    Column {
        dataFiles.forEach { dataFile ->
            ListItem(
                headlineContent = {
                    MiddleEllipsizeMultilineText(
                        modifier =
                            modifier
                                .padding(zeroPadding)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .focusable(false)
                                .semantics {
                                    this.contentDescription = "$fileDescription ${dataFile.fileName}"
                                }
                                .testTag("dataFileItemName"),
                        text = dataFile.fileName,
                        maxLines = 4,
                        textColor = MaterialTheme.colorScheme.onBackground.toArgb(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingContent = {
                    Icon(
                        modifier =
                            modifier
                                .padding(vertical = XSPadding)
                                .size(iconSizeXXS)
                                .wrapContentHeight(align = Alignment.CenterVertically),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_attach_file_48dp_wght400),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier =
                    modifier
                        .padding(vertical = screenViewMediumPadding)
                        .clickable { onClick(dataFile) },
            )
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DataFileItemPreview() {
    RIADigiDocTheme {
        DataFileItem(
            dataFiles = listOf(),
            onClick = {},
        )
    }
}
