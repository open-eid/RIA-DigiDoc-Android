// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.exception

class WrongPasswordException(
    cause: Throwable = Throwable("Wrong password"),
) : CryptoException("Wrong password", cause)
