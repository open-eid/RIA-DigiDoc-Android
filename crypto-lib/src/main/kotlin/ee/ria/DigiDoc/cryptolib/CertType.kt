// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

enum class CertType {
    UnknownType,
    IDCardType,
    DigiIDType,
    EResidentType,
    MobileIDType,
    SmartIDType,
    ESealType,
    PasswordType,
}

fun certType(policies: List<String>): CertType {
    for (oid in policies) {
        when {
            // DigiIDType
            oid.startsWith("1.3.6.1.4.1.51361.1.1.3") ||
                oid.startsWith("1.3.6.1.4.1.51361.1.1.4") ||
                oid.startsWith("1.3.6.1.4.1.51361.2.1.6") ||
                oid.contains("1.3.6.1.4.1.51361.2.1.6") ->
                return CertType.DigiIDType

            // IDCardType
            oid.startsWith("1.3.6.1.4.1.51361.1.1") ||
                oid.startsWith("1.3.6.1.4.1.51361.1.2") ||
                oid.startsWith("1.3.6.1.4.1.51361.2.1") ||
                oid.contains("1.3.6.1.4.1.51361.2.1") ||
                oid.startsWith("1.3.6.1.4.1.51455.1.1") ||
                oid.startsWith("1.3.6.1.4.1.51455.1.2") ||
                oid.startsWith("1.3.6.1.4.1.51455.2.1") ||
                oid.contains("1.3.6.1.4.1.51455.2.1") ->
                return CertType.IDCardType

            // MobileIDType
            oid.startsWith("1.3.6.1.4.1.10015.1.3") ||
                oid.startsWith("1.3.6.1.4.1.10015.11.1") ->
                return CertType.MobileIDType

            // ESealType
            oid.startsWith("1.3.6.1.4.1.10015.7.3") ||
                oid.startsWith("1.3.6.1.4.1.10015.7.1") ||
                oid.startsWith("1.3.6.1.4.1.10015.2.1") ->
                return CertType.ESealType
        }
    }

    return CertType.UnknownType
}
