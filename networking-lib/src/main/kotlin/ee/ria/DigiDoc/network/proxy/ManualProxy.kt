// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.proxy

data class ManualProxy(
    var host: String,
    var port: Int,
    var username: String,
    var password: String,
)
