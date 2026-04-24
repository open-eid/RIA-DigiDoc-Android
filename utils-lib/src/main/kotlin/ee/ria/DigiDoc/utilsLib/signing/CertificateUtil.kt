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

package ee.ria.DigiDoc.utilsLib.signing

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object CertificateUtil {
    @Throws(CertificateException::class)
    fun x509Certificate(certPem: String): X509Certificate =
        CertificateFactory
            .getInstance("X.509")
            .generateCertificate(
                ByteArrayInputStream(certPem.trim().toByteArray(StandardCharsets.UTF_8)),
            ) as X509Certificate

    @Throws(CertificateException::class)
    fun x509Certificate(certDer: ByteArray?): X509Certificate =
        CertificateFactory
            .getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(certDer)) as X509Certificate
}
