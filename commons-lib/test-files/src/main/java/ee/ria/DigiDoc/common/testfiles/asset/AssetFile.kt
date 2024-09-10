@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.testfiles.asset

import android.content.Context
import androidx.annotation.RawRes
import java.io.File
import java.nio.file.Files

open class AssetFile {
    companion object {
        fun getAssetFileAsFile(
            context: Context,
            fileName: String,
        ): File {
            val tempAssetsDir = Files.createTempDirectory("test_assets").toFile()
            val file = File(tempAssetsDir, fileName)

            return context.assets.open(fileName).use { inputStream ->
                file.parentFile?.mkdirs()
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file
            }
        }

        fun getResourceFileAsFile(
            context: Context,
            fileName: String,
            @RawRes resourceId: Int,
        ): File {
            val tempResourcesDir = Files.createTempDirectory("test_resources").toFile()
            val file = File(tempResourcesDir, fileName)

            return context.resources.openRawResource(resourceId).use { inputStream ->
                file.parentFile?.mkdirs()
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file
            }
        }
    }
}
