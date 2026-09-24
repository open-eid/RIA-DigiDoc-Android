// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DiagnosticsText(
    modifier: Modifier = Modifier,
    testTag: String,
    @StringRes labelRes: Int,
    value: String,
    textAlign: TextAlign = TextAlign.Start,
) {
    val label = stringResource(id = labelRes)
    val annotatedString =
        buildAnnotatedString {
            append(label)
            append(value)
        }

    Text(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                    contentDescription = label.lowercase() + " " + value.lowercase()
                }.testTag(testTag)
                .padding(
                    start = screenViewLargePadding,
                    top = screenViewSmallPadding,
                    end = screenViewLargePadding,
                ),
        text = annotatedString,
        textAlign = textAlign,
        style = MaterialTheme.typography.bodyLarge,
    )
}
