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

package ee.ria.DigiDoc.utilsLib.extensions

import com.google.common.base.Splitter
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

fun ByteArray.hexString(): String {
    val hexString = Hex.toHexString(this)
    val formattedHex = Splitter.fixedLength(2).split(hexString)
    return formattedHex.joinToString(separator = " ").trim()
}

@Throws(CertificateException::class)
fun ByteArray.x509Certificate(): X509Certificate? =
    try {
        CertificateFactory
            .getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(this)) as X509Certificate
    } catch (ce: CertificateException) {
        null
    }
