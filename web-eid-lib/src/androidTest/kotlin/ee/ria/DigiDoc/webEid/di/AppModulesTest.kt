// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
