@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import android.content.Context
import ee.ria.DigiDoc.configuration.parser.XmlParser.readSequenceNumber
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil.createDirectoryIfNotExist
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object TSLUtil {
    private val logTag = javaClass.simpleName

    // Copy every TSL file from APKs assets into cache if non-existent
    fun setupTSLFiles(context: Context) {
        val destination: String = File(context.cacheDir, "schema").path
        val assetsPath = "tslFiles"
        var tslFiles: Array<String>? = null
        try {
            tslFiles = context.assets.list(assetsPath)
        } catch (ioe: IOException) {
            errorLog(logTag, "Failed to get folder list: $assetsPath", ioe)
        }

        if (!tslFiles.isNullOrEmpty()) {
            createDirectoryIfNotExist(destination)
            for (fileName in tslFiles) {
                if (isXMLFile(fileName) && shouldCopyTSL(context, assetsPath, fileName, destination)) {
                    copyTSLFromAssets(context, assetsPath, fileName, destination)
                    removeExistingETag(File(destination, fileName).path)
                }
            }
        }
    }

    private fun isXMLFile(filename: String): Boolean {
        return filename.endsWith(".xml")
    }

    private fun shouldCopyTSL(
        context: Context,
        sourcePath: String,
        fileName: String,
        destionationDir: String,
    ): Boolean {
        if (!FileUtil.fileExists(File(destionationDir, fileName).path)) {
            return true
        } else {
            try {
                context.assets.open(File(sourcePath, fileName).path)
                    .use { assetsTSLInputStream ->
                        FileInputStream(File(destionationDir, fileName))
                            .use { cachedTSLInputStream ->
                                val assetsTslVersion: Int =
                                    readSequenceNumber(assetsTSLInputStream)
                                val cachedTslVersion: Int =
                                    readSequenceNumber(cachedTSLInputStream)
                                return assetsTslVersion > cachedTslVersion
                            }
                    }
            } catch (e: Exception) {
                val message = "Error comparing sequence number between assets and cached TSLs"
                errorLog(logTag, message, e)
                return false
            }
        }
    }

    private fun copyTSLFromAssets(
        context: Context,
        sourcePath: String,
        fileName: String,
        destionationDir: String,
    ) {
        try {
            BufferedReader(
                InputStreamReader(
                    context.assets.open(File(sourcePath, fileName).path),
                    StandardCharsets.UTF_8,
                ),
            ).use { reader ->
                FileUtil.writeToFile(reader, destionationDir, fileName)
            }
        } catch (ioe: IOException) {
            errorLog(logTag, "Failed to copy file: $fileName from assets", ioe)
        }
    }

    private fun removeExistingETag(filePath: String) {
        val eTagPath = "$filePath.etag"
        FileUtil.removeFile(eTagPath)
    }
}
