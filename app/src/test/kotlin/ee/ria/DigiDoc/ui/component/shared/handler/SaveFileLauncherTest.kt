// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.shared.handler

import org.junit.Assert.assertEquals
import org.junit.Test

class SaveFileLauncherTest {
    @Test
    fun openMode_truncatesDocumentKnownToHaveContent() {
        assertEquals("wt", openMode(30L))
    }

    @Test
    fun openMode_keepsDefaultModeForEmptyDocument() {
        assertEquals("w", openMode(0L))
    }

    @Test
    fun openMode_keepsDefaultModeWhenSizeIsUnknown() {
        assertEquals("w", openMode(-1L))
    }
}
