@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.snackbar

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object SnackBarManager {
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: SharedFlow<List<String>> = _messages

    fun showMessage(message: String) {
        CoroutineScope(Main).launch {
            _messages.value = _messages.value + message
        }
    }

    fun showMessage(
        context: Context,
        @StringRes message: Int,
    ) {
        CoroutineScope(Main).launch {
            _messages.value = _messages.value + context.getString(message)
        }
    }

    fun removeMessage(message: String) {
        _messages.value = _messages.value.filter { it != message }
    }
}
