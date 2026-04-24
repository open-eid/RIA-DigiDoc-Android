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

package ee.ria.DigiDoc.utilsLib.mimetype

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File

class MimeTypeResolverTest {
    private lateinit var context: Context

    @Mock
    private lateinit var mockMimeTypeCache: MimeTypeCache

    private lateinit var mimeTypeResolver: MimeTypeResolver

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mimeTypeResolver = MimeTypeResolverImpl(mockMimeTypeCache)
    }

    @Test
    fun mimeTypeResolver_mimeType_success() {
        val file = File("testfile.txt")
        val expectedMimeType = "text/plain"

        `when`(mockMimeTypeCache.getMimeType(file)).thenReturn(expectedMimeType)

        val result = mimeTypeResolver.mimeType(file)

        assertEquals(expectedMimeType, result)
        verify(mockMimeTypeCache).getMimeType(file)
    }

    @Test
    fun mimeTypeResolver_mimeType_returnDefaultMimetypeWhenFileHasEmptyMimetype() {
        val file = File("testfile.txt")

        `when`(mockMimeTypeCache.getMimeType(file)).thenReturn("")

        val result = mimeTypeResolver.mimeType(file)

        assertEquals(DEFAULT_MIME_TYPE, result)
        verify(mockMimeTypeCache).getMimeType(file)
    }

    @Test
    fun mimeTypeResolver_mimeType_returnNullWhenFileNull() {
        val result = mimeTypeResolver.mimeType(null)

        assertNull(result)
    }
}
