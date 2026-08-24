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

import android.content.Context
import ee.ria.DigiDoc.common.Constant.TSL_SEQUENCE_NUMBER_ELEMENT
import ee.ria.DigiDoc.configuration.exception.TSLException
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil.createDirectoryIfNotExist
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit

object TSLUtil {
    private val logTag = javaClass.simpleName

    // Copy every TSL file from APKs assets into cache if non-existent
    @Throws(IOException::class)
    fun setupTSLFiles(context: Context) {
        val destination: String = File(context.cacheDir, "schema").path
        val assetsPath = "tslFiles"
        val tslFiles: Array<String>?
        try {
            tslFiles = context.assets.list(assetsPath)
        } catch (ioe: IOException) {
            errorLog(logTag, "Failed to get folder list: $assetsPath", ioe)
            throw ioe
        }

        createDirectoryIfNotExist(destination)

        debugLog(logTag, "Setting up TSL files in cache; bundled in assets: ${tslFiles?.joinToString() ?: "none"}")
        if (!tslFiles.isNullOrEmpty()) {
            for (fileName in tslFiles) {
                if (isXMLFile(fileName) && shouldCopyTSL(context, assetsPath, fileName, destination)) {
                    val tslFile = File(destination, fileName)
                    try {
                        copyTSLFromAssets(context, assetsPath, fileName, destination)
                    } catch (ioe: IOException) {
                        errorLog(logTag, "Failed to copy TSL '$fileName' from assets into cache", ioe)
                        continue
                    }
                    try {
                        setFileDateAttributes(tslFile)
                    } catch (e: Exception) {
                        errorLog(
                            logTag,
                            "Unable to backdate TSL '$fileName'; libdigidocpp will only look for a newer " +
                                "list once the cached copy is a day old",
                            e,
                        )
                    }
                    removeExistingETag(tslFile.path)
                    debugLog(logTag, "Copied TSL '$fileName' from assets into cache (${tslFile.length()} bytes)")
                }
            }
        }
    }

    private fun isXMLFile(filename: String): Boolean = filename.endsWith(".xml")

    @Suppress("SameParameterValue")
    private fun shouldCopyTSL(
        context: Context,
        sourcePath: String,
        fileName: String,
        destinationDir: String,
    ): Boolean {
        val cachedFile = File(destinationDir, fileName)
        if (!FileUtil.fileExists(cachedFile.path)) {
            debugLog(logTag, "TSL '$fileName' is not in the cache yet; copying it from assets")
            return true
        }

        val assetsTslVersion =
            try {
                context.assets.open(File(sourcePath, fileName).path).use { readSequenceNumber(it) }
            } catch (e: Exception) {
                errorLog(logTag, "Unable to read the bundled TSL '$fileName'; keeping the cached copy", e)
                return false
            }

        val cachedTslVersion =
            try {
                FileInputStream(cachedFile).use { readSequenceNumber(it) }
            } catch (e: Exception) {
                errorLog(logTag, "Cached TSL '$fileName' cannot be parsed; replacing it from assets", e)
                return true
            }

        val isAssetNewer = assetsTslVersion > cachedTslVersion
        debugLog(
            logTag,
            "TSL '$fileName': assets version $assetsTslVersion, cached version $cachedTslVersion, " +
                if (isAssetNewer) "updating cache" else "cache is up to date",
        )
        return isAssetNewer
    }

    @Suppress("SameParameterValue")
    @Throws(IOException::class)
    private fun copyTSLFromAssets(
        context: Context,
        sourcePath: String,
        fileName: String,
        destinationDir: String,
    ) {
        BufferedReader(
            InputStreamReader(
                context.assets.open(File(sourcePath, fileName).path),
                StandardCharsets.UTF_8,
            ),
        ).use { reader ->
            FileUtil.writeToFile(reader, destinationDir, fileName)
        }
    }

    private fun removeExistingETag(filePath: String) {
        val eTagPath = "$filePath.etag"
        FileUtil.removeFile(eTagPath)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readSequenceNumber(tslInputStream: InputStream?): Int {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(tslInputStream, null)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == TSL_SEQUENCE_NUMBER_ELEMENT) {
                return parser.nextText().toInt()
            }
            eventType = parser.next()
        }
        throw TSLException("Error reading version from TSL")
    }

    private fun setFileDateAttributes(file: File) {
        val path = file.toPath()

        val currentFileAttrs =
            Files.readAttributes(path, BasicFileAttributes::class.java)

        val sevenDaysAgoMs = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        val fileTime = FileTime.fromMillis(sevenDaysAgoMs)

        val attrs = Files.getFileAttributeView(path, BasicFileAttributeView::class.java)

        attrs.setTimes(fileTime, fileTime, fileTime)

        val updatedFileAttrs =
            Files.readAttributes(path, BasicFileAttributes::class.java)

        debugLog(
            logTag,
            "Changed file ${file.name} modified date attribute " +
                "${currentFileAttrs.lastModifiedTime()} -> ${updatedFileAttrs.lastModifiedTime()}",
        )
    }
}
