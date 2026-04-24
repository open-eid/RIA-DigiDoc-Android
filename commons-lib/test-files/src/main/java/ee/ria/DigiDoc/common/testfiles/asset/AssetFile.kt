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
