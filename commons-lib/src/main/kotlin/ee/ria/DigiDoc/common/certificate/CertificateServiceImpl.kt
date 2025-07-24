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

package ee.ria.DigiDoc.common.certificate

import ee.ria.DigiDoc.common.model.EIDType
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateServiceImpl
    @Inject
    constructor() : CertificateService {
        override fun parseCertificate(data: ByteArray): X509CertificateHolder = X509CertificateHolder(data)

        override fun extractEIDType(certificate: X509CertificateHolder): EIDType {
            val extensions = certificate.extensions
            val certificatePolicies = CertificatePolicies.fromExtensions(extensions)
            return EIDType.parse(certificatePolicies)
        }

        override fun extractKeyUsage(certificate: X509CertificateHolder): KeyUsage {
            val extensions = certificate.extensions
            return KeyUsage.fromExtensions(extensions)
        }

        override fun extractExtendedKeyUsage(certificate: X509CertificateHolder): ExtendedKeyUsage {
            val extensions = certificate.extensions
            var extendedKeyUsage = ExtendedKeyUsage.fromExtensions(extensions)
            if (extendedKeyUsage == null) {
                extendedKeyUsage = ExtendedKeyUsage(arrayOf<KeyPurposeId?>())
            }

            return extendedKeyUsage
        }

        override fun extractFriendlyName(certificate: X509CertificateHolder): String =
            CertificateParsingUtil.extractFriendlyName(certificate)

        override fun isEllipticCurve(certificate: X509CertificateHolder): Boolean =
            certificate.subjectPublicKeyInfo.algorithm.algorithm
                .equals(X9ObjectIdentifiers.id_ecPublicKey)
    }
