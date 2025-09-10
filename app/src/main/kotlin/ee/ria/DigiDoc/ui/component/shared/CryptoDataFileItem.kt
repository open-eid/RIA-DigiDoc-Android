@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.extensions.notAccessible
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CryptoDataFileItem(
    modifier: Modifier = Modifier,
    dataFiles: List<File>,
    isMoreOptionsButtonShown: Boolean = true,
    onClick: (File) -> Unit,
    onDataFileMoreOptionsActionButtonClick: (File) -> Unit,
) {
    val context = LocalContext.current
    val fileDescription = stringResource(R.string.file)

    val buttonName = stringResource(id = R.string.button_name)

    Column {
        dataFiles.forEachIndexed { index, dataFile ->
            Card(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clickable(enabled = isMoreOptionsButtonShown) { onClick(dataFile) }
                        .semantics {
                            this.contentDescription = "$fileDescription ${index + 1} ${dataFile.name} $buttonName"
                            testTagsAsResourceId = true
                        }.testTag("dataFileItemContainer"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = buttonRoundedCornerShape,
            ) {
                Column(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(vertical = SPadding)
                            .padding(start = SPadding, end = XSPadding),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier =
                                modifier
                                    .wrapContentHeight()
                                    .notAccessible(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector =
                                    ImageVector.vectorResource(
                                        id = R.drawable.ic_m3_attach_file_48dp_wght400,
                                    ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier =
                                    modifier
                                        .padding(XSPadding)
                                        .size(iconSizeXXS)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }.testTag("dataFileItemIcon")
                                        .notAccessible(),
                            )
                        }

                        Spacer(modifier = modifier.width(SPadding))

                        Column(modifier = modifier.weight(1f)) {
                            MiddleEllipsizeMultilineText(
                                modifier =
                                    modifier
                                        .padding(zeroPadding)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .focusable(false)
                                        .semantics {
                                            this.contentDescription =
                                                "$fileDescription ${index + 1} ${dataFile.name}"
                                            testTagsAsResourceId = true
                                        }.testTag("dataFileItemName"),
                                text = dataFile.name,
                                maxLines = 4,
                                textColor = MaterialTheme.colorScheme.onSurface.toArgb(),
                                textStyle =
                                    TextStyle(
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }
                        if (isMoreOptionsButtonShown) {
                            IconButton(onClick = { onDataFileMoreOptionsActionButtonClick(dataFile) }) {
                                Icon(
                                    modifier =
                                        modifier
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }.testTag("dataFileItemMoreOptionsIconButton"),
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_more_vert),
                                    contentDescription = "$fileDescription ${index + 1} ${
                                        stringResource(
                                            R.string.more_options,
                                        )
                                    } $buttonName",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CryptoDataFileItemPreview() {
    RIADigiDocTheme {
        CryptoDataFileItem(
            dataFiles = listOf(),
            onClick = {},
            onDataFileMoreOptionsActionButtonClick = {},
        )
    }
}
