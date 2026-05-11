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

package ee.ria.DigiDoc.libdigidoclib.utils

import android.content.Context
import android.content.res.Resources.NotFoundException
import ee.ria.DigiDoc.libdigidoclib.R
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

object FileUtils {
    private const val LIBDIGIDOC_FILEUTILS_LOG_TAG = "Libdigidoc-FileUtils"

    /**
     * Sub-directory name in [cache dir][Context.getCacheDir] for schema.
     */
    private const val SCHEMA_DIR = "schema"

    fun getSchemaDir(context: Context): File {
        val cacheDir = context.cacheDir ?: throw IllegalArgumentException("Cache directory is null")
        val schemaDir = File(cacheDir, SCHEMA_DIR)
        if (schemaDir.mkdirs()) {
            debugLog(
                LIBDIGIDOC_FILEUTILS_LOG_TAG,
                "Directories created or already exist for ${schemaDir.path}",
            )
        }
        return schemaDir
    }

    fun getSchemaPath(context: Context): String = getSchemaDir(context).absolutePath

    @Throws(IOException::class, NotFoundException::class)
    fun initSchema(context: Context) {
        val schemaDir: File = getSchemaDir(context)
        val schemaResourceInputStream: InputStream
        try {
            schemaResourceInputStream = context.resources.openRawResource(R.raw.schema)
        } catch (nfe: NotFoundException) {
            errorLog(LIBDIGIDOC_FILEUTILS_LOG_TAG, "Unable to get 'schema' resource", nfe)
            throw nfe
        }
        schemaResourceInputStream.use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry: ZipEntry?
                while (zipInputStream.nextEntry.also { entry = it } != null) {
                    val entryName = entry?.name ?: throw ZipException("Zip entry name is null")
                    val entryFile = File(schemaDir, entryName)
                    if (!isChild(schemaDir, entryFile)) {
                        throw ZipException("Bad zip entry: $entryName")
                    }
                    Files.copy(zipInputStream, Paths.get(entryFile.toURI()), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    private fun isChild(
        parent: File,
        potentialChild: File,
    ): Boolean =
        runCatching {
            require(potentialChild.toPath().normalize().startsWith(parent.toPath())) {
                "Invalid path: ${potentialChild.canonicalPath}"
            }
            true
        }.getOrElse { false }
}
