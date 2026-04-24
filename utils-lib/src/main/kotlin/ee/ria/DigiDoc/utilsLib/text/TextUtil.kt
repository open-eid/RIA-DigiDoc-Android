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

package ee.ria.DigiDoc.utilsLib.text

import org.apache.commons.lang3.StringUtils
import java.util.stream.Collectors

object TextUtil {
    fun removeEmptyStrings(strings: List<String>?): List<String> {
        val stringList = strings?.let { ArrayList(it) }
        if (stringList != null) {
            return stringList
                .stream()
                .filter { s: String? ->
                    !StringUtils.isBlank(
                        s,
                    )
                }.collect(Collectors.toList())
        }
        return emptyList()
    }

    fun splitTextAndJoin(
        text: String?,
        delimiter: String?,
        joinDelimiter: String?,
    ): String? {
        if (text == null || delimiter == null || joinDelimiter == null) return text

        val nameComponents = text.split(delimiter)
        return nameComponents.joinToString(joinDelimiter)
    }

    fun removeSlashes(text: String): String = text.replace("\\", "")
}
