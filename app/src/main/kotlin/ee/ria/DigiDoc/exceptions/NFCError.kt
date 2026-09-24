// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.exceptions

sealed class NFCError {
    abstract val message: Int

    data class TagLost(
        override val message: Int,
    ) : NFCError()

    data class WrongPin(
        val pinType: String,
        val retriesLeft: Int,
        override val message: Int,
    ) : NFCError()

    data class PinBlocked(
        val pinType: String,
        override val message: Int,
    ) : NFCError()

    data class ApduResponse(
        override val message: Int,
    ) : NFCError()

    data class WrongCan(
        override val message: Int,
    ) : NFCError()

    data class LimitExceeded(
        override val message: Int,
    ) : NFCError()

    data class NoInternetConnection(
        override val message: Int,
    ) : NFCError()

    data class NoProxyConnection(
        override val message: Int,
    ) : NFCError()

    data class NoLockFound(
        override val message: Int,
    ) : NFCError()

    data class CertificateRevoked(
        override val message: Int,
    ) : NFCError()

    data class CertificateUnknown(
        override val message: Int,
    ) : NFCError()

    data class TechnicalError(
        override val message: Int,
    ) : NFCError()

    data class GeneralError(
        override val message: Int,
    ) : NFCError()
}
