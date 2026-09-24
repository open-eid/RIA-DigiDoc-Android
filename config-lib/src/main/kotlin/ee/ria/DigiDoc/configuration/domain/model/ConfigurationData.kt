// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.domain.model

data class ConfigurationData(
    val configurationJson: String?,
    val configurationSignaturePublicKey: String?,
    val configurationSignature: ByteArray?,
)
