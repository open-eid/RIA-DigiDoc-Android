// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import android.content.Context
import ee.ria.DigiDoc.R
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import java.security.cert.X509Certificate

enum class MyEidDocumentStatus {
    VALID,
    EXPIRED,
    UNKNOWN,
}

fun MyEidDocumentStatus.getLocalized(context: Context): String =
    when (this) {
        MyEidDocumentStatus.VALID -> context.getString(R.string.myeid_status_valid)
        MyEidDocumentStatus.EXPIRED -> context.getString(R.string.myeid_status_expired)
        MyEidDocumentStatus.UNKNOWN -> context.getString(R.string.myeid_status_unknown)
    }

fun certificateStatus(x509Certificate: X509Certificate?): MyEidDocumentStatus {
    if (x509Certificate == null) {
        return MyEidDocumentStatus.UNKNOWN
    }
    return try {
        x509Certificate.checkValidity()
        MyEidDocumentStatus.VALID
    } catch (cee: CertificateExpiredException) {
        MyEidDocumentStatus.EXPIRED
    } catch (cnyve: CertificateNotYetValidException) {
        MyEidDocumentStatus.UNKNOWN
    }
}
