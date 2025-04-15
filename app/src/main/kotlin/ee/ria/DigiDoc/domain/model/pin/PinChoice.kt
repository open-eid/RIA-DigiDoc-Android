@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.pin

enum class PinChoice {
    PIN1,
    PIN2,
    PUK,
}

sealed class PinChangeVariant {
    object ChangePin1 : PinChangeVariant()

    object ChangePin2 : PinChangeVariant()

    object ChangePuk : PinChangeVariant()

    object ForgotPin1 : PinChangeVariant()

    object ForgotPin2 : PinChangeVariant()
}
