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
