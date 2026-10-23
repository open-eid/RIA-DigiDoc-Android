// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.accessibility

import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextRange
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.readCharacterByCharacter
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.spokenDates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Locale

class AccessibilityUtilTest {
    @Test
    fun readCharacterByCharacter_separatesEveryCharacterWithInvisibleSeparator() {
        val config = SemanticsConfiguration()

        config.readCharacterByCharacter("+3725")

        assertEquals("+​3​7​2​5", config[SemanticsProperties.EditableText].text)
    }

    @Test
    fun readCharacterByCharacter_mapsCursorAtEndToEndOfSeparatedText() {
        val config = SemanticsConfiguration()

        config.readCharacterByCharacter("123", TextRange(3))

        assertEquals(TextRange(5), config[SemanticsProperties.TextSelectionRange])
    }

    @Test
    fun readCharacterByCharacter_mapsCursorInsideTextToJustAfterThatCharacter() {
        val config = SemanticsConfiguration()

        config.readCharacterByCharacter("12345", TextRange(2))

        assertEquals(TextRange(3), config[SemanticsProperties.TextSelectionRange])
    }

    @Test
    fun readCharacterByCharacter_keepsCursorAtStartAtStart() {
        val config = SemanticsConfiguration()

        config.readCharacterByCharacter("123", TextRange(0))

        assertEquals(TextRange(0), config[SemanticsProperties.TextSelectionRange])
    }

    @Test
    fun readCharacterByCharacter_mapsSelectionSpanBothEnds() {
        val config = SemanticsConfiguration()

        config.readCharacterByCharacter("123456", TextRange(1, 4))

        assertEquals(TextRange(1, 7), config[SemanticsProperties.TextSelectionRange])
    }

    @Test
    fun readCharacterByCharacter_leavesSemanticsUntouchedWhenTextIsEmpty() {
        val config = SemanticsConfiguration()

        config.readCharacterByCharacter("")

        assertFalse(SemanticsProperties.EditableText in config)
        assertFalse(SemanticsProperties.TextSelectionRange in config)
    }

    @Test
    fun spokenDates_writesMonthOutDayFirstInEstonian() {
        val spoken = spokenDates("Kehtiv kuni 01.02.2026", "d. MMMM yyyy", Locale.forLanguageTag("et"))

        assertEquals("Kehtiv kuni 1. veebruar 2026", spoken)
    }

    @Test
    fun spokenDates_writesMonthOutDayFirstInEnglish() {
        val spoken = spokenDates("Valid until 01.02.2026", "d MMMM yyyy", Locale.ENGLISH)

        assertEquals("Valid until 1 February 2026", spoken)
    }

    @Test
    fun spokenDates_keepsTimeAndZoneAfterTheDate() {
        val spoken = spokenDates("01.02.2026 14:05:33 +0200", "d MMMM yyyy", Locale.ENGLISH)

        assertEquals("1 February 2026 14:05:33 +0200", spoken)
    }

    @Test
    fun spokenDates_replacesEveryDateInTheText() {
        val spoken = spokenDates("01.02.2026 - 31.12.2030", "d MMMM yyyy", Locale.ENGLISH)

        assertEquals("1 February 2026 - 31 December 2030", spoken)
    }

    @Test
    fun spokenDates_leavesImpossibleDateUnchanged() {
        val spoken = spokenDates("31.02.2026", "d MMMM yyyy", Locale.ENGLISH)

        assertEquals("31.02.2026", spoken)
    }

    @Test
    fun spokenDates_leavesTextWithoutDatesUnchanged() {
        val spoken = spokenDates("libdigidocpp 4.5.0, serial 12.345", "d MMMM yyyy", Locale.ENGLISH)

        assertEquals("libdigidocpp 4.5.0, serial 12.345", spoken)
    }
}
