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

package ee.ria.DigiDoc.root

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File

class RootCheckerTest {
    @Mock
    private lateinit var mockFile: File

    private lateinit var rootChecker: RootChecker

    private lateinit var rootCheckerWithDirs: RootChecker

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        rootChecker = RootCheckerImpl()
        rootCheckerWithDirs = RootCheckerImpl(listOf(mockFile))
    }

    @Test
    fun rootChecker_isRootRelatedDirectory_returnTrue() {
        `when`(mockFile.exists()).thenReturn(true)

        assertTrue(rootChecker.isRootRelatedDirectory(mockFile))
    }

    @Test
    fun rootChecker_isRootRelatedDirectory_returnFalse() {
        `when`(mockFile.exists()).thenReturn(false)

        assertFalse(rootChecker.isRootRelatedDirectory(mockFile))
    }

    @Test
    fun rootChecker_isRooted_returnTrueWhenRootRelatedFolderExists() {
        `when`(mockFile.path).thenReturn("/sbin")
        `when`(mockFile.exists()).thenReturn(true)

        assertTrue(rootCheckerWithDirs.isRooted())
    }

    @Test
    fun rootChecker_isRooted_returnFalseWhenRootRelatedFolderDoesNotExist() {
        `when`(mockFile.path).thenReturn("/sbin")
        `when`(mockFile.exists()).thenReturn(false)

        assertFalse(rootCheckerWithDirs.isRooted())
    }
}
