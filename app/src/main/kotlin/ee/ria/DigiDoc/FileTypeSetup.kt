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

package ee.ria.DigiDoc

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeSetup
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        fun initializeApplicationFileTypesAssociation(componentClassName: String) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val isOpenAllTypesEnabled =
                sharedPreferences.getBoolean(
                    context.getString(R.string.main_settings_open_all_filetypes_key),
                    true,
                )

            val pm = context.packageManager
            val openAllTypesComponent = ComponentName(context.packageName, "$componentClassName.OPEN_ALL_FILE_TYPES")
            val openCustomTypesComponent = ComponentName(context.packageName, "$componentClassName.OPEN_CUSTOM_TYPES")

            if (isOpenAllTypesEnabled) {
                pm.setComponentEnabledSetting(
                    openAllTypesComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
                pm.setComponentEnabledSetting(
                    openCustomTypesComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            } else {
                pm.setComponentEnabledSetting(
                    openCustomTypesComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
                pm.setComponentEnabledSetting(
                    openAllTypesComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
