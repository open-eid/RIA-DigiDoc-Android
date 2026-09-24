// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
