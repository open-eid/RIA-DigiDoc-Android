@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

enum class UseCase(
    val useCaseName: String,
) {
    SIGN("SIGN"),
    AUTH("AUTH"),
    DECRYPT("DECRYPT"),
    ;

    companion object {
        fun fromName(mode: String): UseCase {
            return UseCase.entries.find { it.useCaseName == mode } ?: SIGN
        }
    }
}
