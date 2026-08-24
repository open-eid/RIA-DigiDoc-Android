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
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.writeBytes

object ConfigurationCache {
    private const val LOG_TAG = "ConfigurationCache"
    private const val TEMPORARY_SUFFIX = ".tmp"
    private const val BACKUP_SUFFIX = ".bak"

    @Throws(IOException::class)
    fun cacheConfigurationFiles(
        context: Context,
        confData: String,
        publicKey: String,
        signature: ByteArray,
    ) {
        val configDir = File(context.cacheDir, CACHE_CONFIG_FOLDER).toPath()
        try {
            Files.createDirectories(configDir)
        } catch (e: IOException) {
            errorLog(LOG_TAG, "Unable to create the configuration cache directory: $configDir", e)
            throw e
        }

        val files =
            listOf(
                CACHED_CONFIG_JSON to confData.toByteArray(StandardCharsets.UTF_8),
                CACHED_CONFIG_PUB to publicKey.toByteArray(StandardCharsets.UTF_8),
                CACHED_CONFIG_RSA to signature,
            )
        val temporaryFiles = mutableListOf<Path>()
        val backups = mutableMapOf<Path, Path>()
        val replaced = mutableListOf<Path>()

        try {
            files.forEach { (fileName, data) ->
                val temporaryFile = createTempFile(configDir, fileName, TEMPORARY_SUFFIX)
                temporaryFiles.add(temporaryFile)
                temporaryFile.writeBytes(data)
            }

            files.forEach { (fileName, _) ->
                val destination = configDir.resolve(fileName)
                if (destination.exists()) {
                    val backup = createTempFile(configDir, fileName, BACKUP_SUFFIX)
                    destination.copyTo(backup, overwrite = true)
                    backups[destination] = backup
                }
            }

            files.forEachIndexed { index, (fileName, _) ->
                val destination = configDir.resolve(fileName)
                temporaryFiles[index].moveTo(destination, overwrite = true)
                replaced.add(destination)
            }
            infoLog(LOG_TAG, "Cached the configuration, its public key and its signature")
        } catch (e: Exception) {
            errorLog(LOG_TAG, "Unable to cache the configuration files, restoring the previous ones", e)
            restorePreviousFiles(replaced, backups)
            throw e
        } finally {
            (temporaryFiles + backups.values).forEach(::deleteLeftover)
        }
    }

    internal fun restorePreviousFiles(
        replaced: List<Path>,
        backups: Map<Path, Path>,
    ) {
        replaced.forEach(::deleteLeftover)
        backups.forEach { (destination, backup) ->
            try {
                backup.moveTo(destination, overwrite = true)
            } catch (e: IOException) {
                errorLog(LOG_TAG, "Unable to restore the previous configuration file: ${destination.name}", e)
            }
        }
    }

    private fun deleteLeftover(path: Path) {
        try {
            path.deleteIfExists()
        } catch (e: IOException) {
            errorLog(LOG_TAG, "Unable to delete the leftover file: ${path.name}", e)
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
