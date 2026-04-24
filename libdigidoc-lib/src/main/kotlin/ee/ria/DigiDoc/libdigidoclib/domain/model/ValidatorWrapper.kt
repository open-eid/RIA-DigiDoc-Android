/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.libdigidocpp.Signature

class ValidatorWrapper(
    validator: Signature.Validator,
) : ValidatorInterface {
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
