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
