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

package ee.ria.DigiDoc.webEid.utils

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey

@RunWith(AndroidJUnit4::class)
class WebEidAlgorithmUtilTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var ecPublicKey256: ECPublicKey
    private lateinit var ecPublicKey384: ECPublicKey

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        val keyGen256 =
            KeyPairGenerator.getInstance("EC").apply {
                initialize(256)
            }
        ecPublicKey256 = keyGen256.generateKeyPair().public as ECPublicKey

        val keyGen384 =
            KeyPairGenerator.getInstance("EC").apply {
                initialize(384)
            }
        ecPublicKey384 = keyGen384.generateKeyPair().public as ECPublicKey
    }

    @Test
    fun buildSupportedSignatureAlgorithms_returnsAllSupportedHashFunctions() {
        val result: JSONArray = WebEidAlgorithmUtil.buildSupportedSignatureAlgorithms(ecPublicKey256)
        assertEquals(8, result.length())
        val first = result.getJSONObject(0)
        assertEquals("ECC", first.getString("cryptoAlgorithm"))
        assertEquals("NONE", first.getString("paddingScheme"))
        assertTrue(first.has("hashFunction"))
    }

    @Test
    fun getAlgorithm_returnsCorrectAlgorithmForKeyLength() {
        val alg256 = WebEidAlgorithmUtil.getAlgorithm(ecPublicKey256)
        val alg384 = WebEidAlgorithmUtil.getAlgorithm(ecPublicKey384)
        assertEquals("ES256", alg256)
        assertEquals("ES384", alg384)
    }

    @Test
    fun buildSupportedSignatureAlgorithms_unsupportedKeyType_throwsException() {
        val rsaKey =
            KeyPairGenerator
                .getInstance("RSA")
                .apply {
                    initialize(2048)
                }.generateKeyPair()
                .public

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidAlgorithmUtil.buildSupportedSignatureAlgorithms(rsaKey)
            }
        assertTrue(exception.message!!.contains("Unsupported key type"))
    }

    @Test
    fun getAlgorithm_unsupportedKeyType_throwsException() {
        val rsaKey =
            KeyPairGenerator
                .getInstance("RSA")
                .apply {
                    initialize(2048)
                }.generateKeyPair()
                .public

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidAlgorithmUtil.getAlgorithm(rsaKey)
            }
        assertTrue(exception.message!!.contains("Unsupported key type"))
    }

    @Test
    fun buildSignatureAlgorithm_sha256_returnsCorrectAlgorithmObject() {
        val result =
            WebEidAlgorithmUtil.buildSignatureAlgorithm(ecPublicKey256, "SHA-256")

        assertEquals("ECC", result.getString("cryptoAlgorithm"))
        assertEquals("SHA-256", result.getString("hashFunction"))
        assertEquals("NONE", result.getString("paddingScheme"))
    }

    @Test
    fun buildSignatureAlgorithm_sha384_returnsCorrectAlgorithmObject() {
        val result =
            WebEidAlgorithmUtil.buildSignatureAlgorithm(ecPublicKey384, "SHA-384")

        assertEquals("ECC", result.getString("cryptoAlgorithm"))
        assertEquals("SHA-384", result.getString("hashFunction"))
        assertEquals("NONE", result.getString("paddingScheme"))
    }

    @Test
    fun buildSignatureAlgorithm_rsaKey_returnsRsaAlgorithmObject() {
        val rsaKey =
            KeyPairGenerator
                .getInstance("RSA")
                .apply { initialize(2048) }
                .generateKeyPair()
                .public

        val result = WebEidAlgorithmUtil.buildSignatureAlgorithm(rsaKey, "SHA-256")

        assertEquals("RSA", result.getString("cryptoAlgorithm"))
        assertEquals("SHA-256", result.getString("hashFunction"))
        assertEquals("PKCS1.5", result.getString("paddingScheme"))
    }
}
