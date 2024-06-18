@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.extensions

import android.text.TextUtils
import com.google.common.base.Splitter
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

fun ByteArray.hexString(): String =
    TextUtils
        .join(
            " ",
            Splitter
                .fixedLength(2)
                .split(Hex.toHexString(this)),
        ).trim { it <= ' ' }

@Throws(CertificateException::class)
fun ByteArray.x509Certificate(): X509Certificate? =
    try {
        CertificateFactory
            .getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(this)) as X509Certificate
    } catch (ce: CertificateException) {
        null
    }
