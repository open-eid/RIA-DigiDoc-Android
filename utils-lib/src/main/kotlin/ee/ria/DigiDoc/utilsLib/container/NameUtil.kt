@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import ee.ria.DigiDoc.utilsLib.text.TextUtil

object NameUtil {
    fun formatName(name: String): String {
        val nameComponents = name.split(",").map { it.trim() }

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

    private fun capitalizeName(name: String): String {
        return name.lowercase()
            .replace(Regex("([\\p{L}\\d])([\\p{L}\\d]*)")) { matchResult ->
                matchResult.groupValues[1].uppercase() + matchResult.groupValues[2]
            }
    }
}
