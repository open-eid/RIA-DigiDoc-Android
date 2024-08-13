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
        @ApplicationContext private val context: Context,
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
