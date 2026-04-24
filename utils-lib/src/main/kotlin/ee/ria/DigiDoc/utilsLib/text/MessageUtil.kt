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

    fun escape(text: String?): String = StringEscapeUtils.escapeJava(text)

    private fun unEscape(text: String?): String = StringEscapeUtils.unescapeJava(text)

    fun toJsonString(`object`: Any?): String {
        val gson: Gson =
            Gson()
                .newBuilder()
                .disableHtmlEscaping()
                .disableInnerClassSerialization()
                .create()
        return unEscape(gson.toJson(`object`))
    }
}
