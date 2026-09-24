// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.shared.keyboard

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch

fun interface KeyboardScrollAdapter {
    suspend fun scrollBy(delta: Float)
}

fun ScrollState.asKeyboardAdapter() =
    KeyboardScrollAdapter { delta ->
        scrollBy(delta)
    }

fun LazyListState.asKeyboardAdapter() =
    KeyboardScrollAdapter { delta ->
        scrollBy(delta)
    }

fun Modifier.keyboardScrollable(
    adapter: KeyboardScrollAdapter,
    step: Float = 100f,
): Modifier =
    composed {
        var heightPx by remember { mutableFloatStateOf(0f) }
        val scope = rememberCoroutineScope()

        this
            .onSizeChanged { heightPx = it.height.toFloat() }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when (event.key) {
                    Key.DirectionDown -> {
                        scope.launch { adapter.scrollBy(step) }
                        true
                    }

                    Key.DirectionUp -> {
                        scope.launch { adapter.scrollBy(-step) }
                        true
                    }

                    Key.PageDown -> {
                        scope.launch { adapter.scrollBy(heightPx) }
                        true
                    }

                    Key.PageUp -> {
                        scope.launch { adapter.scrollBy(-heightPx) }
                        true
                    }

                    else -> false
                }
            }
    }

fun Modifier.keyboardScrollable(
    scrollState: ScrollState,
    step: Float = 100f,
): Modifier = keyboardScrollable(scrollState.asKeyboardAdapter(), step)

fun Modifier.keyboardScrollable(
    listState: LazyListState,
    step: Float = 100f,
): Modifier = keyboardScrollable(listState.asKeyboardAdapter(), step)
