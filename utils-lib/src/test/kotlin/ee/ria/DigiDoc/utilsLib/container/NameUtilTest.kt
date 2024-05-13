@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import org.junit.Assert.assertEquals
import org.junit.Test

class NameUtilTest {
    @Test
    fun nameUtil_formatName_success() {
        val name = "Doe"

        val formattedName = NameUtil.formatName(name)

        assertEquals("Doe", formattedName)
    }

    @Test
    fun nameUtil_formatName_successWithFullName() {
        val name = "Doe, John"

        val formattedName = NameUtil.formatName(name)

        assertEquals("Doe, John", formattedName)
    }

    @Test
    fun nameUtil_formatName_successWithMultipleNames() {
        val name = "Doe, John, Jane"

        val formattedName = NameUtil.formatName(name)

        assertEquals("Doe, John, Jane", formattedName)
    }

    @Test
    fun nameUtil_formatName_returnEmptyStringWithEmptyName() {
        val name = ""

        val formattedName = NameUtil.formatName(name)

        assertEquals("", formattedName)
    }

    @Test
    fun nameUtil_formatName_returnExtraSpacesTrimmed() {
        val name = "  "

        val formattedName = NameUtil.formatName(name)

        assertEquals(" ", formattedName)
    }
}
