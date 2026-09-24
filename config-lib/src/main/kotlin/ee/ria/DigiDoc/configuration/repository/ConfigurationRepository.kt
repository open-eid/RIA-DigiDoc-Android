// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider

interface ConfigurationRepository {
    fun getConfiguration(): ConfigurationProvider?

    suspend fun observeConfigurationUpdates(onUpdate: suspend (ConfigurationProvider) -> Unit)
}
