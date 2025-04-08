@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.extensions

import androidx.compose.foundation.focusable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser

fun Modifier.notAccessible(): Modifier {
    return this
        .focusProperties { canFocus = false }
        .focusable(false)
        .clearAndSetSemantics { invisibleToUser() }
}
