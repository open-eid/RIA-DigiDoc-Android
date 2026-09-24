// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

enum class IdentityAction(
    val useCaseName: String,
) {
    SIGN("SIGN"),
    AUTH("AUTH"),
    DECRYPT("DECRYPT"),
    CERTIFICATE("CERTIFICATE"),
}
