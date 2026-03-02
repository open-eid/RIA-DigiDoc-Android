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

package ee.ria.DigiDoc.webEid.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import ee.ria.DigiDoc.webEid.WebEidAuthServiceImpl
import ee.ria.DigiDoc.webEid.WebEidSignServiceImpl
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppModulesTest {
    private lateinit var modules: AppModules

    @Before
    fun setup() {
        modules = AppModules()
    }

    @Test
    fun provideWebEidAuthService_returnsCorrectImpl() {
        val service = modules.provideWebEidAuthService()
        assertNotNull(service)
        assertTrue(service is WebEidAuthServiceImpl)
    }

    @Test
    fun provideWebEidSignService_returnsCorrectImpl() {
        val service = modules.provideWebEidSignService()
        assertNotNull(service)
        assertTrue(service is WebEidSignServiceImpl)
    }
}
