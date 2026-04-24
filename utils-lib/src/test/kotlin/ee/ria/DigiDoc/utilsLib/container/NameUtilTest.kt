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

        assertEquals("John Doe, Jane", formattedName)
    }

    @Test
    fun nameUtil_formatName_successWithMultipleNamesAndTrimmedSpaces() {
        val input = "Doe ,  John , 123"
        val expectedOutput = "John Doe, 123"
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
        val expectedOutput = "John Doe, 123"
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
        val expectedOutput = "John Doe, 123"
        val result = formatName(input)
        assertEquals(expectedOutput, result)
    }
}
