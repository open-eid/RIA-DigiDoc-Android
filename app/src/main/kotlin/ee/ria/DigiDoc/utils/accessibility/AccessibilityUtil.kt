// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
import android.view.accessibility.AccessibilityManager

class AccessibilityUtil {
    companion object {
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

        // Workaround for TalkBack to announce characters one-by-one (eg. personal code)
        // This adds zero-width space between each number / character
        fun addInvisibleElement(text: String): String {
            val noInvisibleElement = removeInvisibleElement(text)
            return noInvisibleElement.chunked(1).joinToString("\u200B")
        }

        fun removeInvisibleElement(text: String): String = text.replace("\u200B", "")
    }
}
