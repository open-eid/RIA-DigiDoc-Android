@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.text

import com.google.gson.Gson
import org.apache.commons.text.StringEscapeUtils
import threegpp.charset.gsm.GSMCharset
import threegpp.charset.ucs2.UCS2Charset80
import java.nio.charset.Charset

object MessageUtil {
    val GSM_CHARSET: Charset = GSMCharset()
    val UCS2_CHARSET: Charset = UCS2Charset80()

    fun trimDisplayMessageIfNotWithinSizeLimit(
        displayMessage: String,
        maxDisplayMessageBytes: Int,
        charset: Charset?,
    ): String {
        val displayMessagesBytes = charset?.let { displayMessage.toByteArray(it) }
        if (displayMessagesBytes != null) {
            if (displayMessagesBytes.size > maxDisplayMessageBytes) {
                val bytesPerChar =
                    displayMessagesBytes.size.toDouble() / displayMessage.length.toDouble()
                return displayMessage.substring(
                    0,
                    ((maxDisplayMessageBytes - 4) / bytesPerChar).toInt(),
                ) + "..."
            }
        }
        return displayMessage
    }

    fun escape(text: String?): String {
        return StringEscapeUtils.escapeJava(text)
    }

    private fun unEscape(text: String?): String {
        return StringEscapeUtils.unescapeJava(text)
    }

    fun toJsonString(`object`: Any?): String {
        val gson: Gson =
            Gson().newBuilder().disableHtmlEscaping()
                .disableInnerClassSerialization().create()
        return unEscape(gson.toJson(`object`))
    }
}
