@file:Suppress("PackageName")

package ee.ria.DigiDoc

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.preference.PreferenceManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FileTypeSetupTest {
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    private lateinit var fileTypeSetup: FileTypeSetup

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.packageName).thenReturn("com.example.package")
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockContext.packageManager).thenReturn(mockPackageManager)
        `when`(
            mockContext.getString((R.string.main_settings_open_all_filetypes_key)),
        ).thenReturn("main_settings_open_all_filetypes_key")
        `when`(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockSharedPreferences)

        fileTypeSetup = FileTypeSetup(mockContext)
    }

    @Test
    fun fileTypeSetup_initializeApplicationFileTypesAssociation_successEnablingAllFileTypes() {
        `when`(mockSharedPreferences.getBoolean("main_settings_open_all_filetypes_key", true)).thenReturn(true)

        val openAllTypesComponent = ComponentName("com.example.package", "MyComponentClassName.OPEN_ALL_FILE_TYPES")
        val openCustomTypesComponent = ComponentName("com.example.package", "MyComponentClassName.OPEN_CUSTOM_TYPES")

        fileTypeSetup.initializeApplicationFileTypesAssociation("MyComponentClassName")

        verify(mockPackageManager).setComponentEnabledSetting(
            eq(openAllTypesComponent),
            eq(PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
            eq(PackageManager.DONT_KILL_APP),
        )

        verify(mockPackageManager).setComponentEnabledSetting(
            eq(openCustomTypesComponent),
            eq(PackageManager.COMPONENT_ENABLED_STATE_DISABLED),
            eq(PackageManager.DONT_KILL_APP),
        )
    }

    @Test
    fun fileTypeSetup_initializeApplicationFileTypesAssociation_successDisablingAllFileTypes() {
        `when`(mockSharedPreferences.getBoolean("main_settings_open_all_filetypes_key", true)).thenReturn(false)

        val openAllTypesComponent = ComponentName("com.example.package", "MyComponentClassName.OPEN_ALL_FILE_TYPES")
        val openCustomTypesComponent = ComponentName("com.example.package", "MyComponentClassName.OPEN_CUSTOM_TYPES")

        fileTypeSetup.initializeApplicationFileTypesAssociation("MyComponentClassName")

        verify(mockPackageManager).setComponentEnabledSetting(
            eq(openCustomTypesComponent),
            eq(PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
            eq(PackageManager.DONT_KILL_APP),
        )

        verify(mockPackageManager).setComponentEnabledSetting(
            eq(openAllTypesComponent),
            eq(PackageManager.COMPONENT_ENABLED_STATE_DISABLED),
            eq(PackageManager.DONT_KILL_APP),
        )
    }
}
