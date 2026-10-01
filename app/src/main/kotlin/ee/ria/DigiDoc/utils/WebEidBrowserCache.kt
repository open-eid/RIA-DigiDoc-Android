// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
