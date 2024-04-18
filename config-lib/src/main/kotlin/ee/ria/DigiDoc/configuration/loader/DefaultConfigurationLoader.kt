@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import android.content.res.AssetManager
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_RSA
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import java.io.IOException

class DefaultConfigurationLoader(private val assetManager: AssetManager) : ConfigurationLoader() {
    override fun loadConfigurationJson(): String {
        return readFileContent(DEFAULT_CONFIG_JSON)
    }

    override fun loadConfigurationSignature(): ByteArray {
        return readFileContentBytes(DEFAULT_CONFIG_RSA)
    }

    override fun loadConfigurationSignaturePublicKey(): String {
        return readFileContent(DEFAULT_CONFIG_PUB)
    }

    private fun readFileContent(filename: String): String {
        try {
            assetManager.open("config/$filename").use { inputStream ->
                return FileUtil.readFileContent(inputStream)
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to read file content '$filename'", e)
        }
    }

    private fun readFileContentBytes(filename: String): ByteArray {
        try {
            assetManager.open("config/$filename").use { inputStream ->
                return FileUtil.readFileContentBytes(inputStream)
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to read file content '$filename'", e)
        }
    }
}
