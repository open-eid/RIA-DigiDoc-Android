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
