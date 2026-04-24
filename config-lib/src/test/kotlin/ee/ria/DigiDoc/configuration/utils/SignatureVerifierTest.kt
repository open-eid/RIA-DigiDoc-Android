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

import ee.ria.DigiDoc.utilsLib.file.FileUtil
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException
import java.util.Optional

class SignatureVerifierTest {
    @Test
    fun verifyValidSignature() {
        val classLoader =
            Optional
                .ofNullable(javaClass.getClassLoader())
                .orElseThrow {
                    IllegalStateException(
                        "Unable to get ClassLoader",
                    )
                }
        try {
            classLoader.getResourceAsStream("config.json").use { configJsonStream ->
                classLoader.getResourceAsStream("config.rsa").use { configSignatureStream ->
                    classLoader
                        .getResourceAsStream("config.pub")
                        .use { configSignaturePublicKeyStream ->
                            val configJson: String =
                                FileUtil.readFileContent(configJsonStream)
                            val configSignature: ByteArray =
                                FileUtil.readFileContentBytes(configSignatureStream)
                            val configSignaturePublicKey: String =
                                FileUtil.readFileContent(configSignaturePublicKeyStream)
                            TestCase.assertTrue(
                                SignatureVerifier.verify(
                                    configSignature,
                                    configSignaturePublicKey,
                                    configJson,
                                ),
                            )
                        }
                }
            }
        } catch (_: IOException) {
            throw IllegalStateException("Unable to read resource")
        }
    }

    @Test
    fun verifyInvalidSignature() {
        val classLoader =
            Optional
                .ofNullable(javaClass.getClassLoader())
                .orElseThrow {
                    IllegalStateException(
                        "Unable to get ClassLoader",
                    )
                }
        try {
            classLoader.getResourceAsStream("config.json").use { configJsonStream ->
                classLoader.getResourceAsStream("config.rsa").use { configSignatureStream ->
                    classLoader
                        .getResourceAsStream("config.pub")
                        .use { configSignaturePublicKeyStream ->
                            val configJson: String =
                                FileUtil.readFileContent(configJsonStream)
                            val configSignature: ByteArray =
                                FileUtil.readFileContentBytes(configSignatureStream)
                            val configSignaturePublicKey: String =
                                FileUtil.readFileContent(configSignaturePublicKeyStream)
                            TestCase.assertFalse(
                                SignatureVerifier.verify(
                                    configSignature,
                                    configSignaturePublicKey,
                                    configJson + "a",
                                ),
                            )
                        }
                }
            }
        } catch (_: IOException) {
            throw IllegalStateException("Unable to read resource")
        }
    }
}
