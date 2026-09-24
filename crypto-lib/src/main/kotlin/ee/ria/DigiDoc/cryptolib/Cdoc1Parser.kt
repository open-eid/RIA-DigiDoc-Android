// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import android.util.Xml
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.Base64

private const val LOG_TAG = "Cdoc1Parser"
private const val X509_CERTIFICATE = "X509Certificate"
private const val ENCRYPTION_PROPERTY = "EncryptionProperty"
private const val NAME_ATTRIBUTE = "Name"
private const val ORIG_FILE = "orig_file"

data class Cdoc1Content(
    val dataFileNames: List<String>,
    val recipientCertificates: List<ByteArray>,
)

object Cdoc1Parser {
    fun parse(inputStream: InputStream): Cdoc1Content {
        debugLog(LOG_TAG, "Parsing CDOC1 XML stream")
        val parser = Xml.newPullParser().apply { setInput(inputStream, null) }
        val dataFileNames = mutableListOf<String>()
        val recipientCertificates = mutableListOf<ByteArray>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.localName) {
                X509_CERTIFICATE -> certificateOf(parser.nextText())?.let(recipientCertificates::add)
                ENCRYPTION_PROPERTY ->
                    if (parser.isOrigFile()) {
                        fileNameOf(parser.nextText())?.let(dataFileNames::add)
                    }
            }
        }
        debugLog(
            LOG_TAG,
            "Parsed CDOC1: ${dataFileNames.size} data file name(s), " +
                "${recipientCertificates.size} recipient certificate(s)",
        )
        return Cdoc1Content(dataFileNames, recipientCertificates)
    }
}

private val XmlPullParser.localName: String
    get() = name.substringAfterLast(':')

private fun XmlPullParser.isOrigFile(): Boolean = getAttributeValue(null, NAME_ATTRIBUTE) == ORIG_FILE

private fun fileNameOf(origFileProperty: String): String? =
    origFileProperty.substringBefore('|').trim().ifEmpty { null }

private fun certificateOf(base64: String): ByteArray? =
    runCatching { Base64.getMimeDecoder().decode(base64) }
        .onFailure { errorLog(LOG_TAG, "Unable to decode recipient certificate", it) }
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }
