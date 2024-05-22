@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.parser

import ee.ria.DigiDoc.configuration.exception.TSLException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream

object XmlParser {
    private val tslSequenceNumberElement = "TSLSequenceNumber"

    @Throws(XmlPullParserException::class, IOException::class)
    fun readSequenceNumber(tslInputStream: InputStream?): Int {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(tslInputStream, null)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == tslSequenceNumberElement) {
                return parser.nextText().toInt()
            }
            eventType = parser.next()
        }
        throw TSLException("Error reading version from TSL")
    }
}
