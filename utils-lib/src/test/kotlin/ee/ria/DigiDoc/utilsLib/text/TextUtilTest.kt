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

import ee.ria.DigiDoc.utilsLib.text.TextUtil.removeEmptyStrings
import ee.ria.DigiDoc.utilsLib.text.TextUtil.removeSlashes
import ee.ria.DigiDoc.utilsLib.text.TextUtil.splitTextAndJoin
import org.junit.Assert.assertEquals
import org.junit.Test

class TextUtilTest {
    @Test
    fun textUtil_removeEmptyStrings_returnListWithValidTextOnly() {
        val input = listOf("example", "", " ", "\t", "text", "with", "empty", "strings")
        val expectedOutput = listOf("example", "text", "with", "empty", "strings")
        val result = removeEmptyStrings(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeEmptyStrings_returnSameListWhenNoInvalidTextExist() {
        val input = listOf("example", "text", "with", "no", "empty", "strings")
        val expectedOutput = input
        val result = removeEmptyStrings(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeEmptyStrings_returnEmptyListWhenInputNull() {
        val input: List<String>? = null
        val expectedOutput = emptyList<String>()
        val result = removeEmptyStrings(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeEmptyStrings_returnEmptyListWhenInputIsEmptyList() {
        val input = emptyList<String>()
        val expectedOutput = emptyList<String>()
        val result = removeEmptyStrings(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_splitTextAndJoin_success() {
        val inputText = "example,text,with,commas"
        val delimiter = ","
        val joinDelimiter = " "
        val expectedOutput = "example text with commas"
        val result = splitTextAndJoin(inputText, delimiter, joinDelimiter)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_splitTextAndJoin_returnNullWhenInputNull() {
        val inputText: String? = null
        val delimiter = ","
        val joinDelimiter = " "
        val expectedOutput: String? = null
        val result = splitTextAndJoin(inputText, delimiter, joinDelimiter)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_splitTextAndJoin_returnOriginalStringWhenDelimiterNull() {
        val inputText = "example,text,with,commas"
        val delimiter: String? = null
        val joinDelimiter = " "
        val expectedOutput = "example,text,with,commas"
        val result = splitTextAndJoin(inputText, delimiter, joinDelimiter)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_splitTextAndJoin_returnOriginalStringWhenJoinDelimiterNull() {
        val inputText = "example,text,with,commas"
        val delimiter = ","
        val joinDelimiter: String? = null
        val expectedOutput = "example,text,with,commas"
        val result = splitTextAndJoin(inputText, delimiter, joinDelimiter)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeSlashes_success() {
        val input = "example\\,text\\,with\\,slashes"
        val expectedOutput = "example,text,with,slashes"
        val result = removeSlashes(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeSlashes_returnOriginalStringWhenWithoutSlashes() {
        val input = "example, text, without, slashes"
        val expectedOutput = "example, text, without, slashes"
        val result = removeSlashes(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeSlashes_returnEmptyStringWhenEmptyInput() {
        val input = ""
        val expectedOutput = ""
        val result = removeSlashes(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeSlashes_returnOriginalStringWhenWithoutCommas() {
        val input = "example text with no commas"
        val expectedOutput = "example text with no commas"
        val result = removeSlashes(input)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun textUtil_removeSlashes_successWithExtraBackslashes() {
        val input = "example\\\\ text\\\\ with\\\\ escaped\\\\ backslashes"
        val expectedOutput = "example text with escaped backslashes"
        val result = removeSlashes(input)
        assertEquals(expectedOutput, result)
    }
}
