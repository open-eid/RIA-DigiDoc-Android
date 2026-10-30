// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

object RecipientUtil {
    fun isSameRecipient(
        recipient: Addressee,
        other: Addressee,
    ): Boolean = recipient.data.contentEquals(other.data)

    fun isRecipientAdded(
        recipient: Addressee,
        addedRecipients: List<Addressee>,
    ): Boolean = addedRecipients.any { isSameRecipient(it, recipient) }
}
