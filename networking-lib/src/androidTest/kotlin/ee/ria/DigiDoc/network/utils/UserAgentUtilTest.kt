@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import ee.ria.DigiDoc.common.BuildVersionProvider
import ee.ria.DigiDoc.network.utils.UserAgentUtil.getUserAgent
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`

class UserAgentUtilTest {
    private lateinit var context: Context
    private lateinit var usbManager: UsbManager
    private lateinit var usbDevice: UsbDevice
    private lateinit var packageManager: PackageManager
    private lateinit var packageInfo: PackageInfo
    private lateinit var buildVersionProvider: BuildVersionProvider

    @Before
    fun setUp() {
        context = spy(Context::class.java)
        usbManager = mock(UsbManager::class.java)
        usbDevice = mock(UsbDevice::class.java)
        packageManager = mock(PackageManager::class.java)

        packageInfo = PackageInfo()
        packageInfo.versionName = "3.0.0"
        packageInfo.longVersionCode = 1234L

        buildVersionProvider = mock(BuildVersionProvider::class.java)
    }

    @Test
    fun getUserAgent_success() {
        val devices: HashMap<String, UsbDevice> = HashMap()
        devices.put("device1", usbDevice)
        `when`(context.packageManager).thenReturn(packageManager)
        `when`(context.packageName).thenReturn("DigiDoc")
        `when`(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(packageInfo)
        `when`(context.getSystemService(Context.USB_SERVICE)).thenReturn(usbManager)
        `when`(usbManager.getDeviceList()).thenReturn(devices)
        `when`(usbDevice.productName).thenReturn("Smart Card Reader 1")
        `when`(buildVersionProvider.getSDKInt()).thenReturn(32)

        val userAgentString = getUserAgent(context, buildVersionProvider)

        assertEquals(
            userAgentString,
            "riadigidoc/3.0.0.1234 (Android ${Build.VERSION.RELEASE}) Lang: en Devices: Smart Card Reader 1",
        )
    }
}
