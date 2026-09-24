// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import java.io.Serializable
import java.security.cert.X509Certificate

data class ArchiveTimestamp(
    val time: String,
    val certificate: X509Certificate,
) : Serializable
