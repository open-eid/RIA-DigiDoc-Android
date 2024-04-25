@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

interface ValidatorInterface {
    val diagnostics: String
    val status: Status

    enum class Status {
        Valid,
        Warning,
        NonQSCD,
        Test,
        Invalid,
        Unknown,
    }
}
