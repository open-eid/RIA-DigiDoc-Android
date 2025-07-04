@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class ParserTest {
    @Test
    fun parseStringValue() {
        val configurationParser = Parser(TEST_JSON)
        assertEquals("1.0.0.5", configurationParser.parseStringValue("TERA-SUPPORTED"))
        assertEquals("20190805110015Z", configurationParser.parseStringValue("META-INF", "DATE"))
    }

    @Test
    fun parseMissingStringValue() {
        assertThrows(
            "Failed to parse parameter 'MISSING-VALUE' from configuration json",
            RuntimeException::class.java,
        ) {
            val configurationParser = Parser(TEST_JSON)
            assertNull(configurationParser.parseStringValue("MISSING-VALUE"))
        }
    }

    @Test
    fun parseStringValues() {
        val configurationParser = Parser(TEST_JSON)
        val certs: List<String> = configurationParser.parseStringValues("TSL-CERTS")
        assertEquals("a", certs[0])
        assertEquals("b", certs[1])
        assertEquals("c", certs[2])
    }

    @Test
    fun parseIntValue() {
        val configurationParser = Parser(TEST_JSON)
        assertSame(93, configurationParser.parseIntValue("META-INF", "SERIAL"))
    }

    @Test
    fun parseStringValuesToMap() {
        val configurationParser = Parser(TEST_JSON)
        val issuers: Map<String, String> =
            configurationParser.parseStringValuesToMap("OCSP-URL-ISSUER")
        assertEquals("http://ocsp.sk.ee", issuers["KLASS3-SK 2010"])
        assertEquals("http://ocsp.sk.ee", issuers["KLASS3-SK 2016"])
        assertEquals("http://demo.sk.ee/ocsp", issuers["TEST of KLASS3-SK 2010"])
    }

    companion object {
        private const val TEST_JSON =
            "{" +
                "  \"TERA-SUPPORTED\": \"1.0.0.5\"," +
                "  \"META-INF\": {" +
                "    \"URL\": \"https://id.eesti.ee/config.json\"," +
                "    \"DATE\": \"20190805110015Z\"," +
                "    \"SERIAL\": 93," +
                "    \"VER\": 1" +
                "  }," +
                "  \"PICTURE-URL\": \"https://sisene.www.eesti.ee/idportaal/portaal.idpilt\"," +
                "  \"TSL-CERTS\": [" +
                "    \"a\"," +
                "    \"b\"," +
                "    \"c\"" +
                "  ]," +
                "  \"OCSP-URL-ISSUER\": {" +
                "    \"KLASS3-SK 2010\": \"http://ocsp.sk.ee\"," +
                "    \"KLASS3-SK 2016\": \"http://ocsp.sk.ee\"," +
                "    \"TEST of KLASS3-SK 2010\": \"http://demo.sk.ee/ocsp\"" +
                "  }" +
                "}"
    }
}
