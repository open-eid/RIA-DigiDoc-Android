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

package ee.ria.DigiDoc.init

import ee.ria.DigiDoc.cryptolib.init.CryptoInitialization
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CryptoInitializationTest {
    companion object {
        private lateinit var cryptoInitialization: CryptoInitialization
    }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        cryptoInitialization = CryptoInitialization()
    }

    @Test
    fun initialization_init_success() {
        try {
            runTest {
                cryptoInitialization.init()
            }
        } catch (_: Exception) {
            fail("No exceptions should be thrown")
        }
    }
}
