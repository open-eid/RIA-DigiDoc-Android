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

package ee.ria.DigiDoc.utils.libdigidoc

import android.content.Context
import ee.ria.DigiDoc.common.Constant.DDOC_STAMPED_VALID_UNTIL_DATE
import ee.ria.DigiDoc.libdigidoclib.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import java.time.ZonedDateTime

object SignatureStatusUtil {
    fun getSignatureStatusText(
        context: Context,
        status: ValidatorInterface.Status,
    ): String =
        when (status) {
            ValidatorInterface.Status.Valid -> context.getString(R.string.signing_container_signature_status_valid)
            ValidatorInterface.Status.Warning ->
                "${context.getString(R.string.signing_container_signature_status_valid)} " +
                    context.getString(R.string.signing_container_signature_status_warning)
            ValidatorInterface.Status.NonQSCD ->
                "${context.getString(R.string.signing_container_signature_status_valid)} " +
                    context.getString(R.string.signing_container_signature_status_non_qscd)
            ValidatorInterface.Status.Invalid -> context.getString(R.string.signing_container_signature_status_invalid)
            else -> context.getString(R.string.signing_container_signature_status_unknown)
        }

    fun getTimestampStatusText(
        context: Context,
        status: ValidatorInterface.Status,
    ): String =
        when (status) {
            ValidatorInterface.Status.Valid -> context.getString(R.string.signing_container_timestamp_status_valid)
            ValidatorInterface.Status.Warning ->
                "${context.getString(R.string.signing_container_timestamp_status_valid)} " +
                    context.getString(R.string.signing_container_signature_status_warning)
            ValidatorInterface.Status.NonQSCD ->
                "${context.getString(R.string.signing_container_timestamp_status_valid)} " +
                    context.getString(R.string.signing_container_signature_status_non_qscd)
            ValidatorInterface.Status.Invalid -> context.getString(R.string.signing_container_timestamp_status_invalid)
            else -> context.getString(R.string.signing_container_timestamp_status_unknown)
        }

    fun isDdocSignatureValid(signature: SignatureInterface): Boolean {
        val inputDate = ZonedDateTime.parse(signature.trustedSigningTime)
        val signatureValidDate = DDOC_STAMPED_VALID_UNTIL_DATE
        return inputDate.isBefore(signatureValidDate)
    }
}
