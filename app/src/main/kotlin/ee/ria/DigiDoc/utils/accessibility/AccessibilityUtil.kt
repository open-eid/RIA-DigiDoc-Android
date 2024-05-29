@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

class AccessibilityUtil {
    companion object {
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

        fun isTalkBackEnabled(context: Context): Boolean {
            return (
                context.getSystemService(
                    Context.ACCESSIBILITY_SERVICE,
                ) as AccessibilityManager
            ).isTouchExplorationEnabled
        }

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
    }
}
