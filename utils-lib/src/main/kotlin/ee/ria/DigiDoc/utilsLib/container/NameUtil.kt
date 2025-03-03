@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import ee.ria.DigiDoc.utilsLib.text.TextUtil

object NameUtil {
    fun formatName(name: String): String {
        val nameComponents = name.split(",").map { it.trim() }

        val formattedNameComponents =
            if (nameComponents.size == 3) {
                val (lastname, firstname, code) = nameComponents
                "${firstname.lowercase().replaceFirstChar { it.uppercaseChar() }} " +
                    "${lastname.lowercase().replaceFirstChar { it.uppercaseChar() }}, $code".trim()
            } else {
                nameComponents.joinToString(separator = ", ").trim()
            }

        // Remove slashes and double spaces
        return TextUtil
            .removeSlashes(formattedNameComponents)
            .replace("\\s+".toRegex(), " ")
    }
}
