// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.crypto

import ee.ria.DigiDoc.common.Constant.Crypto.PASSWORD_MAXIMUM_LENGTH
import ee.ria.DigiDoc.common.Constant.Crypto.PASSWORD_MINIMUM_LENGTH

object PasswordUtil {
    fun isPasswordValid(password: String): Boolean =
        password.length in PASSWORD_MINIMUM_LENGTH..PASSWORD_MAXIMUM_LENGTH &&
            password.any { it.isDigit() } &&
            password.any { it.isUpperCase() } &&
            password.any { it.isLowerCase() }
}
