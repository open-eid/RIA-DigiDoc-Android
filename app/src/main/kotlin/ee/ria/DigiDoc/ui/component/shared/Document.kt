/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers

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
                .padding(vertical = XSPadding)
                .wrapContentHeight()
                .fillMaxWidth()
                .semantics {
                    this.contentDescription = "$documentTitle ${formatNumbers(name).lowercase()}"
                }.focusable(true)
                .clickable(onClick = onItemClick)
                .testTag("recentDocumentsItem"),
    ) {
        val (
            folderIcon,
            documentText,
            removeIcon,
        ) = createRefs()
        Icon(
            modifier =
                modifier
                    .padding(start = SPadding, end = XSPadding)
                    .size(iconSizeXXS)
                    .constrainAs(folderIcon) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
            imageVector = ImageVector.vectorResource(R.drawable.ic_m3_folder_48dp_wght400),
            contentDescription = null,
        )
        MiddleEllipsizeMultilineText(
            modifier =
                modifier
                    .wrapContentSize()
                    .padding(end = iconSizeXXS * 2 + XSPadding * 2 + SPadding * 2)
                    .constrainAs(documentText) {
                        start.linkTo(folderIcon.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }.focusable(false)
                    .testTag("recentDocumentsItemName"),
            text = name,
            maxLines = 4,
            textColor = textColor,
        )
        IconButton(
            modifier =
                modifier
                    .padding(end = SPadding)
                    .size(iconSizeXXS)
                    .constrainAs(removeIcon) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }.testTag("recentDocumentsItemRemoveButton"),
            onClick = onRemoveButtonClick,
            content = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_m3_delete_48dp_wght400),
                    contentDescription = "${
                        stringResource(
                            id = R.string.recent_documents_remove_button,
                        )
                    } ${formatNumbers(name).lowercase()}",
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
