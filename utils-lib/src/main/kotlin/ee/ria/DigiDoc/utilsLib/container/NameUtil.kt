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

    private fun capitalizeName(name: String): String {
        return name.lowercase()
            .replace(Regex("([\\p{L}\\d])([\\p{L}\\d]*)")) { matchResult ->
                matchResult.groupValues[1].uppercase() + matchResult.groupValues[2]
            }
    }
}
