// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import java.io.Serializable

interface ValidatorInterface : Serializable {
    val diagnostics: String
    val status: Status

    enum class Status {
        Valid,
        Warning,
        NonQSCD,
        Test,
        Invalid,
        Unknown,
    }
}
