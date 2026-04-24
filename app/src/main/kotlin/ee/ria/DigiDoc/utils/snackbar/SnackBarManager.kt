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
