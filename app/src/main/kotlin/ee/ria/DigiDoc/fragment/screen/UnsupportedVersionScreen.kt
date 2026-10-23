// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UnsupportedVersionScreen(modifier: Modifier = Modifier) {
    val linkText = stringResource(R.string.unsupported_version_link_text)
    val message = stringResource(R.string.unsupported_version_message, linkText)
    val url = stringResource(R.string.unsupported_version_url)
    val linkColor = MaterialTheme.colorScheme.primary

    val annotatedMessage =
        buildAnnotatedString {
            append(message)
            val linkStart = message.indexOf(linkText)
            if (linkStart >= 0) {
                addLink(
                    LinkAnnotation.Url(
                        url = url,
                        styles =
                            TextLinkStyles(
                                style =
                                    SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                            ),
                    ),
                    linkStart,
                    linkStart + linkText.length,
                )
            }
        }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("unsupportedVersionScreen"),
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding),
        ) {
            Box(
                modifier =
                    modifier
                        .padding(SPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .testTag("scrollView"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = annotatedMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier =
                        modifier
                            .padding(vertical = SPadding)
                            .testTag("unsupportedVersionMessage"),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                InvisibleElement(modifier = modifier)
            }
        }
    }
}
