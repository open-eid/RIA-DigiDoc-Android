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

@file:Suppress("PackageName", "ArrayInDataClass")

package ee.ria.DigiDoc.common.model

import ee.ria.DigiDoc.common.certificate.CertificateService
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import java.io.IOException

data class ExtendedCertificate(
    val type: EIDType,
    val data: ByteArray,
    val keyUsage: KeyUsage,
    val extendedKeyUsage: ExtendedKeyUsage,
    val ellipticCurve: Boolean,
) {
    companion object {
        @Throws(IOException::class)
        fun create(
            data: ByteArray,
            certificateService: CertificateService,
        ): ExtendedCertificate {
            val certificate = certificateService.parseCertificate(data)
            val type = certificateService.extractEIDType(certificate)
            val ellipticCurve = certificateService.isEllipticCurve(certificate)

            val keyUsage = certificateService.extractKeyUsage(certificate)
            val extendedKeyUsage = certificateService.extractExtendedKeyUsage(certificate)

            return ExtendedCertificate(
                type,
                data,
                keyUsage,
                extendedKeyUsage,
                ellipticCurve,
            )
        }
    }
}
