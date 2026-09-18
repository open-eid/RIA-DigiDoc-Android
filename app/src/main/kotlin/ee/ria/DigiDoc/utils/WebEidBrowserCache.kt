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

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object WebEidBrowserCache {
    private val RESOLVED_TTL = 30.seconds
    private val SELECTED_TTL = 5.minutes

    private var origin: String? = null
    private var packageName: String? = null
    private var expiresAt = 0L

    fun rememberResolved(
        origin: String,
        browserPackage: String,
        now: Long = System.currentTimeMillis(),
    ) = remember(origin, browserPackage, now + RESOLVED_TTL.inWholeMilliseconds)

    fun rememberSelected(
        origin: String,
        browserPackage: String,
        now: Long = System.currentTimeMillis(),
    ) = remember(origin, browserPackage, now + SELECTED_TTL.inWholeMilliseconds)

    fun recall(
        origin: String,
        now: Long = System.currentTimeMillis(),
    ): String? {
        val remembered = packageName?.takeIf { this.origin == origin && now <= expiresAt }
        clear()
        return remembered
    }

    private fun clear() {
        origin = null
        packageName = null
        expiresAt = 0L
    }

    private fun remember(
        origin: String,
        browserPackage: String,
        expiry: Long,
    ) {
        this.origin = origin
        packageName = browserPackage
        expiresAt = expiry
    }
}
