@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import javax.inject.Inject

@HiltViewModel
class SignerDetailViewModel
    @Inject
    constructor() : ViewModel() {
        private val logTag = "SignerDetailViewModel"

        fun getIssuerCommonName(x509Certificate: X509Certificate?): String {
            if (x509Certificate == null) {
                errorLog(logTag, "Certificate is null")
                return ""
            }

            return try {
                val x500name = JcaX509CertificateHolder(x509Certificate).issuer
                val cn = x500name.getRDNs(BCStyle.CN).first()
                IETFUtils.valueToString(cn?.first?.value) ?: ""
            } catch (e: CertificateEncodingException) {
                errorLog(logTag, "Unable to get certificate issuer", e)
                ""
            }
        }

        fun getSubjectCommonName(x509Certificate: X509Certificate?): String {
            if (x509Certificate == null) {
                errorLog(logTag, "Certificate is null")
                return ""
            }

            return try {
                val x500name = JcaX509CertificateHolder(x509Certificate).subject
                val cn = x500name.getRDNs(BCStyle.CN).first()
                IETFUtils.valueToString(cn.first.value)
            } catch (e: CertificateEncodingException) {
                errorLog(logTag, "Unable to get certificate subject", e)
                ""
            }
        }
    }
