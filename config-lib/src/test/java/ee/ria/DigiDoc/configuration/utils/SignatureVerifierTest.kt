@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import ee.ria.DigiDoc.utilslib.file.FileUtil
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.Optional

class SignatureVerifierTest {
    @Test
    fun verifyValidSignature() {
        val classLoader =
            Optional.ofNullable(javaClass.getClassLoader())
                .orElseThrow {
                    IllegalStateException(
                        "Unable to get ClassLoader",
                    )
                }
        try {
            classLoader.getResourceAsStream("config.json").use { configJsonStream ->
                classLoader.getResourceAsStream("config.rsa").use { configSignatureStream ->
                    classLoader.getResourceAsStream("config.pub")
                        .use { configSignaturePublicKeyStream ->
                            val configJson: String =
                                FileUtil.readFileContent(configJsonStream)
                            val configSignature: ByteArray =
                                FileUtil.readFileContentBytes(configSignatureStream)
                            val configSignaturePublicKey: String =
                                FileUtil.readFileContent(configSignaturePublicKeyStream)
                            assertTrue(
                                SignatureVerifier.verify(
                                    configSignature,
                                    configSignaturePublicKey,
                                    configJson,
                                ),
                            )
                        }
                }
            }
        } catch (e: IOException) {
            throw IllegalStateException("Unable to read resource")
        }
    }

    @Test
    fun verifyInvalidSignature() {
        val classLoader =
            Optional.ofNullable(javaClass.getClassLoader())
                .orElseThrow {
                    IllegalStateException(
                        "Unable to get ClassLoader",
                    )
                }
        try {
            classLoader.getResourceAsStream("config.json").use { configJsonStream ->
                classLoader.getResourceAsStream("config.rsa").use { configSignatureStream ->
                    classLoader.getResourceAsStream("config.pub")
                        .use { configSignaturePublicKeyStream ->
                            val configJson: String =
                                FileUtil.readFileContent(configJsonStream)
                            val configSignature: ByteArray =
                                FileUtil.readFileContentBytes(configSignatureStream)
                            val configSignaturePublicKey: String =
                                FileUtil.readFileContent(configSignaturePublicKeyStream)
                            assertFalse(
                                SignatureVerifier.verify(
                                    configSignature,
                                    configSignaturePublicKey,
                                    configJson + "a",
                                ),
                            )
                        }
                }
            }
        } catch (e: IOException) {
            throw IllegalStateException("Unable to read resource")
        }
    }
}
