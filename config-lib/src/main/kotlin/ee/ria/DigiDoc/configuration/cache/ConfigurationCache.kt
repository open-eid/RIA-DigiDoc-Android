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
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.infoLog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object ConfigurationCache {
    private const val LOG_TAG = "ConfigurationCache"
    private const val TEMPORARY_SUFFIX = ".tmp"

    @Throws(IOException::class)
    fun cacheConfigurationFiles(
        context: Context,
        confData: String,
        publicKey: String,
        signature: ByteArray,
    ) {
        val configDir = File(context.cacheDir, CACHE_CONFIG_FOLDER)
        if (!configDir.exists() && !configDir.mkdirs()) {
            val message = "Unable to create the configuration cache directory: ${configDir.path}"
            errorLog(LOG_TAG, message)
            throw IOException(message)
        }

        val files =
            listOf(
                CACHED_CONFIG_JSON to confData.toByteArray(StandardCharsets.UTF_8),
                CACHED_CONFIG_PUB to publicKey.toByteArray(StandardCharsets.UTF_8),
                CACHED_CONFIG_RSA to signature,
            )
        val temporaryFiles = mutableListOf<File>()

        try {
            files.forEach { (fileName, data) ->
                val temporaryFile = File.createTempFile(fileName, TEMPORARY_SUFFIX, configDir)
                temporaryFiles += temporaryFile
                FileOutputStream(temporaryFile).use { outputStream -> outputStream.write(data) }
            }
            files.forEachIndexed { index, (fileName, _) ->
                if (!temporaryFiles[index].renameTo(File(configDir, fileName))) {
                    throw IOException("Unable to move the cached configuration file into place: $fileName")
                }
            }
            infoLog(LOG_TAG, "Cached the configuration, its public key and its signature")
        } catch (e: Exception) {
            errorLog(LOG_TAG, "Unable to cache the configuration files, keeping the previous ones", e)
            throw e
        } finally {
            temporaryFiles.filter(File::exists).forEach { temporaryFile ->
                if (!temporaryFile.delete()) {
                    errorLog(LOG_TAG, "Unable to delete the temporary file: ${temporaryFile.name}")
                }
            }
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
        errorLog(LOG_TAG, "Cached configuration file not found: $fileName")
        throw FileNotFoundException()
    }
}
