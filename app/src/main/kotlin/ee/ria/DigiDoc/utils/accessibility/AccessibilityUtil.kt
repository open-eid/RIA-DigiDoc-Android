// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.editableText
import androidx.compose.ui.semantics.textSelectionRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import ee.ria.DigiDoc.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AccessibilityUtil {
    companion object {
        private val numericDate = Regex("""\b(\d{1,2})\.(\d{1,2})\.(\d{4})\b""")

        fun sendAccessibilityEvent(
            context: Context,
            eventText: CharSequence,
            eventType: Int = TYPE_VIEW_ACCESSIBILITY_FOCUSED,
        ) {
            val accessibilityManager =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            if (accessibilityManager.isEnabled && isTalkBackEnabled(context)) {
                val event = AccessibilityEvent(eventType)
                event.text.add(eventText)
                accessibilityManager.sendAccessibilityEvent(event)
            }
        }

        fun isTalkBackEnabled(context: Context): Boolean =
            (
                context.getSystemService(
                    Context.ACCESSIBILITY_SERVICE,
                ) as AccessibilityManager
            ).isTouchExplorationEnabled

        fun formatNumbers(input: String): String {
            val regex = Regex("\\d+|\\D+")

            return regex.findAll(input).joinToString(" ") { matchResult ->
                val match = matchResult.value
                when {
                    match.all { it.isDigit() } -> match.toCharArray().joinToString(" ")
                    else -> match.lowercase()
                }
            }
        }

        fun SemanticsPropertyReceiver.readCharacterByCharacter(
            text: String,
            selection: TextRange = TextRange(text.length),
        ) {
            if (text.isEmpty()) return
            editableText = AnnotatedString(text.toList().joinToString("\u200B"))
            textSelectionRange = TextRange(separatedOffset(selection.start), separatedOffset(selection.end))
        }

        fun spokenDates(
            text: String,
            pattern: String,
            locale: Locale,
        ): String {
            val formatter = DateTimeFormatter.ofPattern(pattern, locale)
            return numericDate.replace(text) { match ->
                val (day, month, year) = match.destructured
                runCatching { LocalDate.of(year.toInt(), month.toInt(), day.toInt()).format(formatter) }
                    .getOrDefault(match.value)
            }
        }

        @Composable
        fun spokenDates(text: String): String =
            spokenDates(text, stringResource(R.string.spoken_date_format), LocalConfiguration.current.locales[0])

        // Every character but the last is followed by a separator, so offset n lands at 2n - 1
        private fun separatedOffset(offset: Int): Int = if (offset == 0) 0 else offset * 2 - 1
    }
}
