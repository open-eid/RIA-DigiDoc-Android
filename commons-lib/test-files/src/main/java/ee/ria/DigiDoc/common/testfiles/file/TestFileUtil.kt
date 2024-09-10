@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.testfiles.file

import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TestFileUtil {
    companion object {
        fun createZipWithTextFile(mimeType: String): File {
            val textFileName = "mimetype"

            val textFile = File.createTempFile(textFileName, "")
            textFile.deleteOnExit()
            FileWriter(textFile).use { writer ->
                writer.write(mimeType)
            }

            val zipFile = File.createTempFile("example", ".zip")
            zipFile.deleteOnExit()
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                val zipEntry = ZipEntry(textFileName)
                zipOut.putNextEntry(zipEntry)

                textFile.inputStream().use { input ->
                    input.copyTo(zipOut)
                }

                zipOut.closeEntry()
            }

            return zipFile
        }
    }
}
