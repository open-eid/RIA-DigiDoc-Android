@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.signing

object UUIDUtil {
    private const val UUID_REGEX =
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"

    fun isValid(stringRepresentation: String?): Boolean {
        return stringRepresentation != null && stringRepresentation.matches(UUID_REGEX.toRegex())
    }
}
