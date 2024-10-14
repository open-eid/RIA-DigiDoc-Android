@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.ui.component.shared.MiddleEllipsizeMultilineText
import ee.ria.DigiDoc.ui.theme.Black
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible

@Composable
fun ContainerFile(
    modifier: Modifier = Modifier,
    dataFile: DataFileInterface,
    showRemoveButton: Boolean,
    onClickView: () -> Unit = {},
    onClickRemove: () -> Unit = {},
    onClickSave: () -> Unit = {},
) {
    val fileText = stringResource(id = R.string.file)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(start = itemSpacingPadding, end = screenViewLargePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onClickView,
                shape = RectangleShape,
                modifier =
                    modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                        .semantics {
                            contentDescription = "${fileText.lowercase()} " +
                                formatNumbers(dataFile.fileName).lowercase()
                        }
                        .testTag("signatureUpdateListDocumentName"),
            ) {
                MiddleEllipsizeMultilineText(
                    text = dataFile.fileName,
                    textColor = Black.toArgb(),
                    maxLines = 4,
                    modifier = modifier.fillMaxWidth().notAccessible(),
                )
            }

            if (showRemoveButton) {
                IconButton(
                    onClick = onClickRemove,
                    modifier =
                        modifier
                            .size(iconSize)
                            .testTag("signatureUpdateListDocumentRemoveButton"),
                    content = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                            contentDescription = "${
                                stringResource(
                                    id = R.string.document_remove_button,
                                )
                            } ${formatNumbers(dataFile.fileName).lowercase()}",
                            tint = Red500,
                        )
                    },
                )
            }

            Spacer(modifier = modifier.width(itemSpacingPadding))

            IconButton(
                onClick = onClickSave,
                modifier =
                    modifier
                        .size(iconSize)
                        .testTag("signatureUpdateListDocumentSaveButton"),
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_save),
                        contentDescription = "${stringResource(
                            id = R.string.document_save_button,
                        )} ${formatNumbers(dataFile.fileName).lowercase()}",
                        tint = Blue500,
                    )
                },
            )
        }
        HorizontalDivider(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenViewLargePadding, vertical = itemSpacingPadding)
                    .height(dividerHeight),
        )
    }
}
