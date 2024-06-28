@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import org.junit.Assert.assertEquals
import org.junit.Test

class NameUtilTest {
    @Test
    fun nameUtil_formatName_success() {
        val name = "Doe"

        val formattedName = formatName(name)

        assertEquals("Doe", formattedName)
    }

    @Test
    fun nameUtil_formatName_successWithFullName() {
        val name = "Doe, John"

        val formattedName = formatName(name)

        assertEquals("Doe, John", formattedName)
    }

    @Test
    fun nameUtil_formatName_successWithMultipleNames() {
        val name = "Doe, John, Jane"

        val formattedName = formatName(name)

        assertEquals("John, Doe, Jane", formattedName)
    }

    @Test
    fun nameUtil_formatName_successWithMultipleNamesAndTrimmedSpaces() {
        val input = "Doe ,  John , 123"
        val expectedOutput = "John, Doe, 123"
        val result = formatName(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun nameUtil_formatName_returnEmptyStringWithEmptyName() {
        val name = ""

        val formattedName = formatName(name)

        assertEquals("", formattedName)
    }

    @Test
    fun nameUtil_formatName_returnExtraSpacesTrimmedWithEmptyInput() {
        val name = "  "

        val formattedName = formatName(name)

        assertEquals("", formattedName)
    }

    @Test
    fun nameUtil_formatName_returnFormattedWithOneComponent() {
        val input = "Doe"
        val expectedOutput = "Doe"
        val result = formatName(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun nameUtil_formatName_returnFormattedWithTwoComponents() {
        val input = "Doe,John"
        val expectedOutput = "Doe, John"
        val result = formatName(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun nameUtil_formatName_returnFormattedWithThreeComponents() {
        val input = "Doe,John,123"
        val expectedOutput = "John, Doe, 123"
        val result = formatName(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun nameUtil_formatName_returnFormattedWithMoreThanThreeComponents() {
        val input = "Doe,John,123,Extra"
        val expectedOutput = "Doe, John, 123, Extra"
        val result = formatName(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun nameUtil_formatName_returnWithoutSlashes() {
        val input = "Doe\\,John\\,123"
        val expectedOutput = "John, Doe, 123"
        val result = formatName(input)
        assertEquals(expectedOutput, result)
    }
}
