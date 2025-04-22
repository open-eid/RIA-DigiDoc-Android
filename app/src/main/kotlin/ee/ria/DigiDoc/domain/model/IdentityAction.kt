@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

enum class IdentityAction(
    val useCaseName: String,
) {
    SIGN("SIGN"),
    AUTH("AUTH"),
    DECRYPT("DECRYPT"),
    ;

    companion object {
        fun fromName(mode: String): IdentityAction {
            return IdentityAction.entries.find { it.useCaseName == mode } ?: SIGN
        }
    }
}
