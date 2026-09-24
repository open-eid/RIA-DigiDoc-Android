// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
