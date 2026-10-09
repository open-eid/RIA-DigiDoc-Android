// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

object VersionUtil {
    fun isLowerThan(
        version: String,
        otherVersion: String?,
    ): Boolean {
        val segments = segments(version)
        val otherSegments = segments(otherVersion.orEmpty())
        if (segments.isEmpty() || otherSegments.isEmpty()) {
            return false
        }

        val length = maxOf(segments.size, otherSegments.size)
        for (index in 0 until length) {
            val segment = segments.getOrElse(index) { 0 }
            val otherSegment = otherSegments.getOrElse(index) { 0 }
            if (segment != otherSegment) {
                return segment < otherSegment
            }
        }
        return false
    }

    private fun segments(version: String): List<Int> =
        version
            .trim()
            .split('.')
            .asSequence()
            .map { it.toIntOrNull() }
            .takeWhile { it != null && it >= 0 }
            .filterNotNull()
            .toList()
}
