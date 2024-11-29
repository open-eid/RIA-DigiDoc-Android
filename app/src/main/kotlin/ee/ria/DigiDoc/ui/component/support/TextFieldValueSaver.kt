@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.support

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

val textFieldValueSaver =
    Saver<TextFieldValue, Any>(
        save = { value ->
            listOf(value.text, value.selection.start, value.selection.end)
        },
        restore = { restored ->
            val (text, start, end) = restored as List<*>
            TextFieldValue(
                text = text as String,
                selection = TextRange(start as Int, end as Int),
            )
        },
    )
