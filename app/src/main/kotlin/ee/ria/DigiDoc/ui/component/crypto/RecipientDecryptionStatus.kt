// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.crypto

enum class RecipientDecryptionStatus {
    NOT_ENCRYPTED,
    NOT_ENCRYPTED_EXPIRED,
    EXPIRED,
    VALID,
}
