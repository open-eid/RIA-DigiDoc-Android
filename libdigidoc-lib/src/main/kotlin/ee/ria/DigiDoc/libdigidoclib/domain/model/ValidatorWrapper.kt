@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.libdigidocpp.Signature

class ValidatorWrapper(validator: Signature.Validator) : ValidatorInterface {
    override val diagnostics: String = validator.diagnostics()

    override val status: ValidatorInterface.Status =
        when (validator.status()) {
            Signature.Validator.Status.Valid -> ValidatorInterface.Status.Valid
            Signature.Validator.Status.Warning -> ValidatorInterface.Status.Warning
            Signature.Validator.Status.NonQSCD -> ValidatorInterface.Status.NonQSCD
            Signature.Validator.Status.Test -> ValidatorInterface.Status.Test
            Signature.Validator.Status.Invalid -> ValidatorInterface.Status.Invalid
            Signature.Validator.Status.Unknown -> ValidatorInterface.Status.Unknown
            else -> ValidatorInterface.Status.Unknown
        }
}
