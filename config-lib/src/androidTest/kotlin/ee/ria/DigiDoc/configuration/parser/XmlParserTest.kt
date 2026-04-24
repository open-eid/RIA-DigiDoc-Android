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
    fun xmlParser_readSequenceNumber_successful() {
        val xml = "<root><TSLSequenceNumber>123</TSLSequenceNumber></root>"
        val inputStream = ByteArrayInputStream(xml.toByteArray())

        val sequenceNumber = XmlParser.readSequenceNumber(inputStream)

        assertEquals(123, sequenceNumber)
    }

    @Test(expected = TSLException::class)
    fun xmlParser_readSequenceNumber_throwTSLExceptionWhenElementNotFound() {
        val xml = "<root><AnotherElement>123</AnotherElement></root>"
        val inputStream = ByteArrayInputStream(xml.toByteArray())

        XmlParser.readSequenceNumber(inputStream)
    }

    @Test(expected = XmlPullParserException::class)
    fun xmlParser_readSequenceNumber_throwXmlPullParserExceptionWithXmlPullParserEventType() {
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
    fun xmlParser_readSequenceNumber_throwIOParserExceptionWithXmlPullParser() {
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
