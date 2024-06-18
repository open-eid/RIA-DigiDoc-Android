@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import ee.ria.DigiDoc.utilsLib.text.TextUtil

object NameUtil {
    fun formatName(name: String): String {
        val nameComponents = name.split(",")

        val formattedNameComponents =
            if (nameComponents.size == 3) {
                val (lastname, firstname, code) = nameComponents
                "$firstname, $lastname, $code".trim()
            } else {
                nameComponents.joinToString(separator = ", ")
            }

        // Remove slashes and double spaces
        return TextUtil
            .removeSlashes(formattedNameComponents)
            .replace("\\s+".toRegex(), " ")
    }
}
