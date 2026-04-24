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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.accessibility

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
import android.view.accessibility.AccessibilityManager

class AccessibilityUtil {
    companion object {
        fun getAccessibilityEventType(): Int =
            if (Build.VERSION.SDK_INT >= 34) {
                TYPE_VIEW_ACCESSIBILITY_FOCUSED
            } else {
                TYPE_ANNOUNCEMENT
            }

        fun sendAccessibilityEvent(
            context: Context,
            eventType: Int,
            eventText: CharSequence,
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
