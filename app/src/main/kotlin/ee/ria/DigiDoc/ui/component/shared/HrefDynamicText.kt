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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle

@Composable
fun HrefDynamicText(
    modifier: Modifier,
    text1: String?,
    text2: String?,
    linkText: String,
    linkUrl: String,
    newLineBeforeLink: Boolean = false,
    newLineBeforeText2: Boolean = false,
    textStyle: TextStyle =
        TextStyle(
            textAlign = TextAlign.Start,
        ),
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.onSecondaryContainer
    val annotatedStringWithLinks =
        createAnnotatedStringWithLinks(
            text1,
            text2,
            linkText,
            linkUrl,
            linkColor,
            newLineBeforeLink,
            newLineBeforeText2,
        )

    val onClick: (Int) -> Unit = { offset ->
        annotatedStringWithLinks
            .getStringAnnotations(tag = "URL", start = offset, end = offset)
            .firstOrNull()
            ?.let { annotation ->
                uriHandler.openUri(annotation.item)
            }
    }

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator =
        Modifier.pointerInput(onClick) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    onClick(layoutResult.getOffsetForPosition(pos))
                }
            }
        }

    val windowInfo = LocalWindowInfo.current
    val containerHeight = with(LocalDensity.current) { windowInfo.containerSize.height.toDp() }

    BasicText(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = containerHeight / 2)
                .verticalScroll(rememberScrollState())
                .testTag("hrefDynamicText")
                .then(pressIndicator)
                .semantics(mergeDescendants = true) {
                    if (!linkUrl.isEmpty()) {
                        contentDescription = "$text1 $text2 $linkText link $linkUrl"
                    }
                }
                .let {
                    val urlInText =
                        annotatedStringWithLinks
                            .getStringAnnotations(tag = "URL", start = 0, end = annotatedStringWithLinks.length)
                            .firstOrNull()
                            ?.item
                    if (!urlInText.isNullOrEmpty()) {
                        it.clickable(enabled = true, onClick = { uriHandler.openUri(urlInText) })
                    } else {
                        it
                    }
                },
        text = annotatedStringWithLinks,
        style = textStyle,
    )
}

fun createAnnotatedStringWithLinks(
    text1: String?,
    text2: String?,
    linkText: String,
    linkUrl: String,
    linkColor: Color,
    newLineBeforeLink: Boolean = false,
    newLineBeforeText2: Boolean = false,
): AnnotatedString =
    buildAnnotatedString {
        text1?.let(::append)

        if (newLineBeforeLink) {
            appendLine()
        } else if (!text1.isNullOrBlank()) {
            append(" ")
        }

        pushStringAnnotation(tag = "URL", annotation = linkUrl)
        withStyle(
            SpanStyle(
                color = linkColor,
                textDecoration = Underline,
            ),
        ) {
            append(linkText)
        }
        pop()

        text2?.let {
            if (newLineBeforeText2) {
                appendLine()
            } else {
                append(" ")
            }
            append(it)
        }
    }
