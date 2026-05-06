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
