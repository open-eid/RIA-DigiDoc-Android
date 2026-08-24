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

package ee.ria.DigiDoc.libdigidoclib.init

import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Libdigidocpp is not thread-safe and clears a trust list before refilling it,
// so every call is confined here
internal val libdigidocppDispatcher: CoroutineDispatcher = IO.limitedParallelism(1)

private val libdigidocppScope = CoroutineScope(SupervisorJob() + libdigidocppDispatcher)

internal fun applyToLibdigidocpp(
    description: String,
    block: suspend () -> Unit,
) {
    libdigidocppScope.launch {
        try {
            block()
            debugLog(LIBDIGIDOC_INIT_LOG_TAG, "Applied $description to libdigidocpp")
        } catch (e: Exception) {
            errorLog(LIBDIGIDOC_INIT_LOG_TAG, "Failed to apply $description to libdigidocpp", e)
        }
    }
}
