// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipientUtilTest {
    @Test
    fun recipientUtil_isRecipientAdded_returnsFalseWhenAddedListIsEmpty() {
        assertFalse(RecipientUtil.isRecipientAdded(recipient(byteArrayOf(1, 2, 3)), listOf()))
    }

    @Test
    fun recipientUtil_isRecipientAdded_returnsTrueWhenCertificateDataMatches() {
        val added = listOf(recipient(byteArrayOf(9)), recipient(byteArrayOf(1, 2, 3)))

        assertTrue(RecipientUtil.isRecipientAdded(recipient(byteArrayOf(1, 2, 3)), added))
    }

    @Test
    fun recipientUtil_isRecipientAdded_returnsFalseWhenCertificateDataDiffers() {
        val added = listOf(recipient(byteArrayOf(1, 2, 3)))

        assertFalse(RecipientUtil.isRecipientAdded(recipient(byteArrayOf(1, 2, 4)), added))
    }

    @Test
    fun recipientUtil_isRecipientAdded_returnsTrueWhenOnlyCertificateDataMatches() {
        val added = listOf(recipient(byteArrayOf(1, 2, 3), identifier = "11111111111"))

        assertTrue(
            RecipientUtil.isRecipientAdded(
                recipient(byteArrayOf(1, 2, 3), identifier = "22222222222"),
                added,
            ),
        )
    }

    @Test
    fun recipientUtil_isRecipientAdded_returnsFalseWhenSamePersonHasAnotherCertificate() {
        val added = listOf(recipient(byteArrayOf(1, 2, 3), identifier = "11111111111"))

        assertFalse(
            RecipientUtil.isRecipientAdded(
                recipient(byteArrayOf(4, 5, 6), identifier = "11111111111"),
                added,
            ),
        )
    }

    @Test
    fun recipientUtil_isSameRecipient_returnsTrueWhenCertificateDataMatchesButIdentifierDiffers() {
        assertTrue(
            RecipientUtil.isSameRecipient(
                recipient(byteArrayOf(1, 2, 3), identifier = "11111111111"),
                recipient(byteArrayOf(1, 2, 3), identifier = "22222222222"),
            ),
        )
    }

    @Test
    fun recipientUtil_isSameRecipient_returnsFalseWhenSamePersonHasAnotherCertificate() {
        assertFalse(
            RecipientUtil.isSameRecipient(
                recipient(byteArrayOf(1, 2, 3), identifier = "11111111111"),
                recipient(byteArrayOf(4, 5, 6), identifier = "11111111111"),
            ),
        )
    }

    private fun recipient(
        data: ByteArray,
        identifier: String = "11111111111",
    ): Addressee =
        Addressee(
            data = data,
            identifier = identifier,
            serialNumber = identifier,
            givenName = "Test",
            surname = "Recipient",
            certType = CertType.IDCardType,
            validTo = null,
            concatKDFAlgorithmURI = null,
        )
}
