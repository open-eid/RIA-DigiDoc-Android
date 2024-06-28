@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {
    @Test
    fun stringExtensions_removeWhitespaces_successWithExtraSpaces() {
        val input = " a b  c   d "
        val expected = "abcd"

        val result = input.removeWhitespaces()

        assertEquals(expected, result)
    }

    @Test
    fun stringExtensions_removeWhitespaces_returnOriginalStringWithSameInput() {
        val input = "abcdef"
        val expected = "abcdef"

        val result = input.removeWhitespaces()

        assertEquals(expected, result)
    }

    @Test
    fun stringExtensions_removeWhitespaces_returnEmptyStringWithEmptyInput() {
        val input = ""
        val expected = ""

        val result = input.removeWhitespaces()

        assertEquals(expected, result)
    }

    @Test
    fun stringExtensions_formatHexString_success() {
        val input = "0123456789abcdef"
        val expected = "01 23 45 67 89 AB CD EF"

        val result = input.formatHexString()

        assertEquals(expected, result)
    }

    @Test
    fun stringExtensions_formatHexString_returnEmptyStringWithEmptyInput() {
        val input = ""
        val expected = ""

        val result = input.formatHexString()

        assertEquals(expected, result)
    }

    @Test
    fun stringExtensions_formatHexString_returnStringAsUppercase() {
        val input = "a"
        val expected = "A"

        val result = input.formatHexString()

        assertEquals(expected, result)
    }

    @Test
    fun stringExtensions_formatHexString_returnSameHexStringWhenAlreadyFormatted() {
        val input = "01 23 45 67 89 AB CD EF"
        val expected = "01 23 45 67 89 AB CD EF"

        val result = input.formatHexString()

        assertEquals(expected, result)
    }

    @Test
    fun stringExtensions_removeDoubleSpaces_successWithExtraDoubleSpaces() {
        val input = "This  is   a    test"
        val expectedOutput = "This is a test"
        val result = input.removeDoubleSpaces()
        assertEquals(expectedOutput, result)
    }

    @Test
    fun stringExtensions_removeDoubleSpaces_successWithNoDoubleSpaces() {
        val input = "This is a test"
        val expectedOutput = "This is a test"
        val result = input.removeDoubleSpaces()
        assertEquals(expectedOutput, result)
    }

    @Test
    fun stringExtensions_removeDoubleSpaces_successWithLeadingAndTrailingDoubleSpaces() {
        val input = "  This is a test  "
        val expectedOutput = " This is a test "
        val result = input.removeDoubleSpaces()
        assertEquals(expectedOutput, result)
    }

    @Test
    fun stringExtensions_removeDoubleSpaces_returnEmptyStringWithEmptyInput() {
        val input = ""
        val expectedOutput = ""
        val result = input.removeDoubleSpaces()
        assertEquals(expectedOutput, result)
    }

    @Test
    fun stringExtensions_removeDoubleSpaces_returnStringWithDoubleSpaces() {
        val input = "     "
        val expectedOutput = " "
        val result = input.removeDoubleSpaces()
        assertEquals(expectedOutput, result)
    }

    @Test
    fun stringExtensions_removeDoubleSpaces_returnStringWithoutNewLinesAndTabs() {
        val input = "This is\ta test\nwith newlines\n\tand\ttabs"
        val expectedOutput = "This is a test with newlines and tabs"
        val result = input.removeDoubleSpaces()
        assertEquals(expectedOutput, result)
    }

    @Test
    fun stringExtensions_removeDoubleSpaces_returnStringWithoutMixedNewLinesTabsAndSpaces() {
        val input = "This is \n a \t test \t\n with mixed \t\n whitespace"
        val expectedOutput = "This is a test with mixed whitespace"
        val result = input.removeDoubleSpaces()
        assertEquals(expectedOutput, result)
    }
}
