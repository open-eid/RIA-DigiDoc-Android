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

package ee.ria.DigiDoc.configuration.properties

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import java.util.Date

interface ConfigurationProperties {
    fun getConfigurationProperties(context: Context): ConfigurationProperty

    fun updateProperties(
        context: Context,
        lastUpdateCheck: Date?,
        lastUpdated: Date?,
        serial: Int?,
    )

    fun getConfigurationUpdatedDate(context: Context): Date?

    fun setConfigurationUpdatedDate(
        context: Context,
        date: Date?,
    )

    fun getConfigurationLastCheckDate(context: Context): Date?

    fun setConfigurationLastCheckDate(
        context: Context,
        date: Date?,
    )

    fun clearConfigurationLastCheckDate(context: Context)

    fun getLastCheckedAppVersion(context: Context): String?

    fun setLastCheckedAppVersion(
        context: Context,
        version: String,
    )

    fun getConfigurationVersionSerial(context: Context): Int?

    fun setConfigurationVersionSerial(
        context: Context,
        serial: Int?,
    )
}
