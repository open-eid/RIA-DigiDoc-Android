// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.pin

sealed class PinChangeVariant {
    object ChangePin1 : PinChangeVariant()

    object ChangePin2 : PinChangeVariant()

    object ChangePuk : PinChangeVariant()

    object ForgotPin1 : PinChangeVariant()

    object ForgotPin2 : PinChangeVariant()
}
