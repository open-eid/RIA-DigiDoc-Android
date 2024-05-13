@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

object NameUtil {
    fun formatName(name: String): String {
        val nameComponents = name.split(",")

        // Remove double spaces
        return nameComponents.joinToString(separator = ", ")
            .replace("\\s+".toRegex(), " ")
    }
}
