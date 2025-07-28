@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

enum class IdentityAction(
    val useCaseName: String,
) {
    SIGN("SIGN"),
    AUTH("AUTH"),
    DECRYPT("DECRYPT"),
}
