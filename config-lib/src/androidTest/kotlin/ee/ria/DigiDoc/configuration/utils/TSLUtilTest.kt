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

package ee.ria.DigiDoc.configuration.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TSLUtilTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        val cacheDir = File(context.cacheDir, "schema")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun tslUtil_setupTSLFiles_success() {
        TSLUtil.setupTSLFiles(context)

        val destinationDir = File(context.cacheDir, "schema")
        assertTrue(destinationDir.exists())
        assertTrue(destinationDir.isDirectory)
    }

    @Test(expected = IOException::class)
    fun tslUtil_setupTSLFiles_throwExceptionWhenCannotAccessAssets() {
        val mockContext = mock(Context::class.java)
        `when`(mockContext.assets).thenAnswer {
            throw IOException("Mocked exception")
        }
        TSLUtil.setupTSLFiles(mockContext)
    }

    @Test
    fun tslUtil_setupTSLFiles_overwriteExistingFileSuccess() {
        val schema = File(context.cacheDir, "schema")
        val eeFile = File(schema, "EE.xml")
        schema.mkdirs()
        eeFile.createNewFile()

        TSLUtil.setupTSLFiles(context)

        assertTrue(eeFile.exists())
        assertTrue(eeFile.isFile)
    }
}
