// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.proxy

import java.io.IOException

class ProxyAuthenticationException(
    message: String,
) : IOException(message)
