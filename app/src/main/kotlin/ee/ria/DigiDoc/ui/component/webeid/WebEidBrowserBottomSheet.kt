// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.webeid

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WebEidBrowserBottomSheet(
    modifier: Modifier = Modifier,
    browsers: List<WebEidBrowser>,
    @StringRes message: Int,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(SPadding)
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag("webEidBrowserSheetContainer"),
        ) {
            Text(
                modifier =
                    modifier
                        .padding(XSPadding)
                        .testTag("webEidBrowserSheetTitle")
                        .notAccessible(),
                text = stringResource(R.string.web_eid_select_browser_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                modifier = modifier.padding(XSPadding).testTag("webEidBrowserSheetMessage"),
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = modifier.padding(XSPadding))
            browsers.forEach { browser ->
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .clickable { onSelect(browser.packageName) }
                            .padding(XSPadding)
                            .testTag("webEidBrowserSheetItem"),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val icon = browser.icon
                    if (icon != null) {
                        androidx.compose.foundation.Image(
                            bitmap = icon,
                            contentDescription = null,
                            modifier = modifier.padding(XSPadding).size(iconSizeXXS).notAccessible(),
                        )
                    } else {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_m3_open_in_new_48dp_wght400),
                            contentDescription = null,
                            modifier = modifier.padding(XSPadding).size(iconSizeXXS).notAccessible(),
                        )
                    }
                    Spacer(modifier = modifier.width(XSPadding))
                    Text(
                        text = browser.label,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
