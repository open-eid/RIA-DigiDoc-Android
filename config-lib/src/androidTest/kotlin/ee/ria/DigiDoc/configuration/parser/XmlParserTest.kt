@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.parser

import ee.ria.DigiDoc.configuration.exception.TSLException
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class XmlParserTest {
    @Test
    fun testReadSequenceNumber_successful() {
        val xml = "<root><TSLSequenceNumber>123</TSLSequenceNumber></root>"
        val inputStream = ByteArrayInputStream(xml.toByteArray())

        val sequenceNumber = XmlParser.readSequenceNumber(inputStream)

        assertEquals(123, sequenceNumber)
    }

    @Test(expected = TSLException::class)
    fun testReadSequenceNumber_elementNotFound() {
        val xml = "<root><AnotherElement>123</AnotherElement></root>"
        val inputStream = ByteArrayInputStream(xml.toByteArray())

        XmlParser.readSequenceNumber(inputStream)
    }

    @Test(expected = XmlPullParserException::class)
    fun testReadSequenceNumber_xmlPullParserException() {
        val inputStream = mock(InputStream::class.java)
        val xmlPullParserFactory = mock(XmlPullParserFactory::class.java)
        val xmlPullParser = mock(XmlPullParser::class.java)

        `when`(xmlPullParserFactory.newPullParser()).thenReturn(xmlPullParser)
        `when`(xmlPullParser.eventType).thenThrow(XmlPullParserException::class.java)

        val parser = xmlPullParserFactory.newPullParser()
        parser.setInput(inputStream, null)
        parser.eventType
        XmlParser.readSequenceNumber(inputStream)
    }

    @Test(expected = IOException::class)
    fun testReadSequenceNumber_ioException() {
        val inputStream = mock(InputStream::class.java)

        val xmlPullParserFactory = mock(XmlPullParserFactory::class.java)
        val xmlPullParser = mock(XmlPullParser::class.java)

        `when`(xmlPullParserFactory.newPullParser()).thenReturn(xmlPullParser)
        `when`(xmlPullParser.next()).thenThrow(IOException::class.java)

        val parser = xmlPullParserFactory.newPullParser()
        parser.setInput(inputStream, null)
        XmlParser.readSequenceNumber(inputStream)
    }
}
