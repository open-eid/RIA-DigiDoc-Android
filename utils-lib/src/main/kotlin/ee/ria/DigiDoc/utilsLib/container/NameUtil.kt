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

package ee.ria.DigiDoc.utilsLib.container

import ee.ria.DigiDoc.utilsLib.text.TextUtil

object NameUtil {
    fun formatName(nameComponents: List<String>): String {
        val formattedNameComponents =
            if (nameComponents.size == 3) {
                val (lastname, firstname, code) = nameComponents
                "${capitalizeName(firstname)} ${capitalizeName(lastname)}, $code".trim()
            } else {
                nameComponents.joinToString(separator = ", ") { capitalizeName(it) }.trim()
            }

        // Remove slashes and double spaces
        return TextUtil
            .removeSlashes(formattedNameComponents)
            .replace("\\s+".toRegex(), " ")
    }

    fun formatName(name: String): String {
        val nameComponents = name.split(",").map { it.trim() }

        return formatName(nameComponents)
    }

    fun formatName(
        surname: String?,
        givenName: String?,
        identifier: String?,
    ): String {
        val nameComponents = mutableListOf<String>()
        surname?.let { nameComponents.add(it) }
        givenName?.let { nameComponents.add(it) }
        identifier?.let { nameComponents.add(it) }

        return formatName(nameComponents)
    }

    fun formatCompanyName(
        identifier: String?,
        serialNumber: String?,
    ): String {
        val nameComponents = mutableListOf<String>()
        identifier?.let { nameComponents.add(it) }
        serialNumber?.let { nameComponents.add(it) }

        return nameComponents
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }

    private fun capitalizeName(name: String): String =
        name
            .lowercase()
            .replace(Regex("([\\p{L}\\d])([\\p{L}\\d]*)")) { matchResult ->
                matchResult.groupValues[1].uppercase() + matchResult.groupValues[2]
            }
}
