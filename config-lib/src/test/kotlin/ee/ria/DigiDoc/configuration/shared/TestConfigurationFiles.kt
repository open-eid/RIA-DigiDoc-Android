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

package ee.ria.DigiDoc.configuration.shared

import ee.ria.DigiDoc.utilsLib.file.FileUtil

internal object TestConfigurationFiles {
    fun config(): String = bytes("config.json").toString(Charsets.UTF_8)

    fun publicKey(): String = text("config.ecpub")

    fun signature(): ByteArray = bytes("config.ecc")

    private fun bytes(name: String): ByteArray =
        classLoader().getResourceAsStream(name).use { FileUtil.readFileContentBytes(it) }

    private fun text(name: String): String =
        classLoader().getResourceAsStream(name).use { FileUtil.readFileContent(it) }

    private fun classLoader(): ClassLoader =
        javaClass.classLoader ?: throw IllegalStateException("Unable to get ClassLoader")
}
