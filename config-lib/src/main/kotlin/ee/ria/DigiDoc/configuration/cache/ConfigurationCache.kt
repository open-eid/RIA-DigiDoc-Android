@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.cache

import android.content.Context
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException

object ConfigurationCache {
    private const val LOG_TAG = "ConfigurationCache"

    fun cacheConfigurationFiles(
        context: Context,
        confData: String,
        publicKey: String,
        signature: ByteArray,
    ) {
        cacheFile(context, CACHED_CONFIG_JSON, confData)
        cacheFile(context, CACHED_CONFIG_PUB, publicKey)
        cacheFile(context, CACHED_CONFIG_RSA, signature)
    }

    private fun cacheFile(
        context: Context,
        fileName: String,
        data: String,
    ) {
        val configDir = File(context.cacheDir, CACHE_CONFIG_FOLDER)
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        val configFile = File(configDir, fileName)
        try {
            FileWriter(configFile).use { writer ->
                writer.write(data)
            }
        } catch (ioe: IOException) {
            LoggingUtil.errorLog(
                LOG_TAG,
                "Unable to cache file $fileName",
                ioe,
            )
        }
    }

    private fun cacheFile(
        context: Context,
        fileName: String,
        data: ByteArray,
    ) {
        val configDir = File(context.cacheDir, CACHE_CONFIG_FOLDER)
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        val configFile = File(configDir, fileName)

        try {
            FileOutputStream(configFile).use { fos ->
                fos.write(data)
            }
        } catch (e: IOException) {
            LoggingUtil.errorLog(LOG_TAG, "Unable to cache file $fileName", e)
        }
    }

    @Throws(FileNotFoundException::class)
    fun getCachedFile(
        context: Context,
        fileName: String,
    ): File {
        val configDir = File(context.cacheDir, CACHE_CONFIG_FOLDER)
        val configFile = File(configDir, fileName)
        if (configFile.exists() && configFile.isFile) {
            return configFile
        }
        throw FileNotFoundException()
    }
}
