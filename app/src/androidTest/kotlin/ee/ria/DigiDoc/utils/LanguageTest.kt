// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Languages.ENGLISH_LANGUAGE
import ee.ria.DigiDoc.utils.Constant.Languages.ESTONIAN_LANGUAGE
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageTest {
    @Test
    fun testLanguages() {
        assertEquals(ENGLISH_LANGUAGE, Language.English.locale)
        assertEquals(ESTONIAN_LANGUAGE, Language.Estonian.locale)
    }
}
