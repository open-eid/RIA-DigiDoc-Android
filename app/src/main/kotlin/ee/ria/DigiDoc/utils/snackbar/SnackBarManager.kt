// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.snackbar

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object SnackBarManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val queue = mutableListOf<SnackBarMessage>()
    private val _currentMessage = MutableStateFlow<SnackBarMessage?>(null)
    val currentMessage: StateFlow<SnackBarMessage?> = _currentMessage.asStateFlow()

    private var isPresenting = false

    fun showMessage(
        text: String,
        type: SnackbarType = SnackbarType.ERROR,
    ) {
        scope.launch {
            val message = SnackBarMessage(text, type)
            if (queue.lastOrNull() == message) return@launch
            queue.add(message)
            processNext()
        }
    }

    fun showMessage(
        context: Context,
        @StringRes resId: Int,
        type: SnackbarType = SnackbarType.ERROR,
    ) {
        showMessage(context.getString(resId), type)
    }

    private fun processNext() {
        if (isPresenting || queue.isEmpty()) return
        isPresenting = true
        _currentMessage.value = queue.removeAt(0)
        scope.launch {
            delay(4_000)
            _currentMessage.value = null
            isPresenting = false
            processNext()
        }
    }
}
