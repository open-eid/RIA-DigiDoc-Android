// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
