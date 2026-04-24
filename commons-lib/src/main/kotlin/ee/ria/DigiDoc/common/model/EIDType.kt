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

package ee.ria.DigiDoc.common.model

import org.bouncycastle.asn1.x509.CertificatePolicies

enum class EIDType {
    UNKNOWN,
    ID_CARD,
    DIGI_ID,
    MOBILE_ID,
    E_SEAL,
    ;

    companion object {
        fun parse(certificatePolicies: CertificatePolicies?): EIDType {
            if (certificatePolicies == null) {
                return UNKNOWN
            }
            for (policyInformation in certificatePolicies.policyInformation) {
                val identifier = policyInformation.policyIdentifier.id
                if (identifier.startsWith("1.3.6.1.4.1.10015.1.1") ||
                    identifier.startsWith("1.3.6.1.4.1.51361.1.1.1")
                ) {
                    return ID_CARD
                } else if (identifier.startsWith("1.3.6.1.4.1.10015.1.2") ||
                    identifier.startsWith("1.3.6.1.4.1.51361.1.1") ||
                    identifier.startsWith("1.3.6.1.4.1.51455.1.1")
                ) {
                    return DIGI_ID
                } else if (identifier.startsWith("1.3.6.1.4.1.10015.1.3") ||
                    identifier.startsWith("1.3.6.1.4.1.10015.11.1")
                ) {
                    return MOBILE_ID
                } else if (identifier.startsWith("1.3.6.1.4.1.10015.7.3") ||
                    identifier.startsWith("1.3.6.1.4.1.10015.7.1") ||
                    identifier.startsWith("1.3.6.1.4.1.10015.2.1")
                ) {
                    return E_SEAL
                }
            }
            return UNKNOWN
        }
    }
}
