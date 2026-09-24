// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import android.net.Uri
import ee.ria.DigiDoc.BuildConfig

enum class WebEidOperation(
    val operation: String,
) {
    AUTH("auth"),
    CERT("cert"),
    SIGN("sign"),
    ;

    companion object {
        fun fromOperation(operation: String): WebEidOperation? = entries.find { it.operation == operation }
    }
}

object WebEidUriUtil {
    private const val CUSTOM_SCHEME = "web-eid-mobile"

    fun isWebEidUri(uri: Uri): Boolean = getOperation(uri) != null

    fun getOperation(uri: Uri): WebEidOperation? {
        val operation =
            when {
                uri.scheme == CUSTOM_SCHEME -> uri.host
                uri.scheme == "https" && uri.host == BuildConfig.APP_LINKS_HOST -> uri.pathSegments.firstOrNull()
                else -> null
            }
        return operation?.let { WebEidOperation.fromOperation(it) }
    }
}
