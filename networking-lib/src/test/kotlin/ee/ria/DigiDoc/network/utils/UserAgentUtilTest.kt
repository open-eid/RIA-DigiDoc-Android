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

package ee.ria.DigiDoc.network.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import ee.ria.DigiDoc.common.BuildVersionProvider
import ee.ria.DigiDoc.network.utils.UserAgentUtil.getAppInfo
import ee.ria.DigiDoc.network.utils.UserAgentUtil.getUserAgent
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Locale

class UserAgentUtilTest {
    private lateinit var context: Context
    private lateinit var usbManager: UsbManager
    private lateinit var usbDevice: UsbDevice
    private lateinit var packageManager: PackageManager
    private lateinit var packageInfo: PackageInfo
    private lateinit var buildVersionProvider: BuildVersionProvider

    private val expectedDeviceModel =
        Build.MODEL
            ?.lowercase()
            ?.replace(" ", "-")
            .orEmpty()

    private val originalLocale = Locale.getDefault()

    @Before
    fun setUp() {
        Locale.setDefault(Locale.ENGLISH)
        UserAgentUtil.resetLibdigidocppVersion()
        context = mock(Context::class.java)
        usbManager = mock(UsbManager::class.java)
        usbDevice = mock(UsbDevice::class.java)
        packageManager = mock(PackageManager::class.java)

        packageInfo = mock(PackageInfo::class.java)
        packageInfo.versionName = "1.2.3"
        `when`(packageInfo.longVersionCode).thenReturn(1234L)

        buildVersionProvider = mock(BuildVersionProvider::class.java)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun userAgentUtil_getUserAgent_returnBaseStringWithNoDiagnostics() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()

        val result = getUserAgent(context, buildVersionProvider = buildVersionProvider)

        assertEquals(
            "APP riadigidoc/1.2.3.1234 (schema=1; os=Android ${Build.VERSION.RELEASE}; lang=en; devicetype=mobile/$expectedDeviceModel)",
            result,
        )
    }

    @Test
    fun userAgentUtil_getUserAgent_notIncludeDevicesWithNoDiagnostics() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()

        val result = getUserAgent(context, SendDiagnostics.None, buildVersionProvider)

        assertFalse(result.contains("devices="))
    }

    @Test
    fun userAgentUtil_getUserAgent_includeLibPrefixWhenLibVersionProvided() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()
        UserAgentUtil.setLibdigidocppVersion("4.5.6.40")

        val expectedArch =
            Build.SUPPORTED_ABIS?.firstOrNull()?.let { abi ->
                when {
                    abi.startsWith("arm64") -> "arm64"
                    abi.startsWith("armeabi") -> "arm"
                    abi.startsWith("x86_64") -> "x86_64"
                    abi.startsWith("x86") -> "x86"
                    else -> abi
                }
            } ?: "unknown"

        val result = getUserAgent(context, buildVersionProvider = buildVersionProvider)

        assertEquals(
            "LIB libdigidocpp/4.5.6.40 ($expectedArch) APP riadigidoc/1.2.3.1234 (schema=1; os=Android ${Build.VERSION.RELEASE}; lang=en; devicetype=mobile/$expectedDeviceModel)",
            result,
        )
    }

    @Test
    fun userAgentUtil_getUserAgent_includeTabletDeviceTypeForWideScreen() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()

        val configuration = Configuration()
        configuration.smallestScreenWidthDp = 700
        val resources = mock(Resources::class.java)
        `when`(resources.configuration).thenReturn(configuration)
        `when`(context.resources).thenReturn(resources)

        val result = getUserAgent(context, buildVersionProvider = buildVersionProvider)

        assertTrue(result.contains("devicetype=tablet/"))
    }

    @Test
    fun userAgentUtil_getUserAgent_returnUnknownAppVersionOnPackageNotFound() {
        `when`(context.packageManager).thenReturn(packageManager)
        `when`(context.packageName).thenReturn("App")
        `when`(packageManager.getPackageInfo(anyString(), anyInt()))
            .thenThrow(PackageManager.NameNotFoundException())
        mockUsbManagerHandling()
        `when`(buildVersionProvider.getSDKInt()).thenReturn(Build.VERSION.SDK_INT)

        val result = getUserAgent(context, buildVersionProvider = buildVersionProvider)

        assertTrue(result.contains("APP riadigidoc/unknown "))
    }

    @Test
    fun userAgentUtil_getUserAgent_includeConnectedDeviceInDevicesMode() {
        val devices = HashMap<String, UsbDevice>()
        devices["device1"] = usbDevice
        mockPackageManagerHandling()
        mockUsbManagerHandling(devices)
        `when`(usbDevice.productName).thenReturn("Smart Card Reader 1")

        val result = getUserAgent(context, SendDiagnostics.Devices, buildVersionProvider)

        assertEquals(
            "APP riadigidoc/1.2.3.1234 (schema=1; os=Android ${Build.VERSION.RELEASE}; lang=en; devicetype=mobile/$expectedDeviceModel; devices=Smart Card Reader 1)",
            result,
        )
    }

    @Test
    fun userAgentUtil_getUserAgent_notIncludeDevicesWhenListEmpty() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()

        val result = getUserAgent(context, SendDiagnostics.Devices, buildVersionProvider)

        assertFalse(result.contains("devices="))
    }

    @Test
    fun userAgentUtil_getUserAgent_notIncludeNonMatchingDevice() {
        val devices = HashMap<String, UsbDevice>()
        devices["device1"] = usbDevice
        mockPackageManagerHandling()
        mockUsbManagerHandling(devices)
        `when`(usbDevice.productName).thenReturn("Keyboard")
        `when`(usbDevice.deviceName).thenReturn("/dev/bus/usb/001/001")

        val result = getUserAgent(context, SendDiagnostics.Devices, buildVersionProvider)

        assertFalse(result.contains("devices="))
    }

    @Test
    fun userAgentUtil_getUserAgent_sanitizeDeviceNameDelimitersAndControlChars() {
        val devices = HashMap<String, UsbDevice>()
        devices["device1"] = usbDevice
        mockPackageManagerHandling()
        mockUsbManagerHandling(devices)
        `when`(usbDevice.productName).thenReturn("Smart;Card(Reader)\r\n1")

        val result = getUserAgent(context, SendDiagnostics.Devices, buildVersionProvider)

        assertTrue(result.contains("devices=SmartCardReader1)"))
        assertFalse(result.contains("\r"))
        assertFalse(result.contains("\n"))
    }

    @Test
    fun userAgentUtil_getUserAgent_includeDeviceMatchedByDeviceName() {
        val devices = HashMap<String, UsbDevice>()
        devices["device1"] = usbDevice
        mockPackageManagerHandling()
        mockUsbManagerHandling(devices)
        `when`(usbDevice.deviceName).thenReturn("/dev/bus/usb/001/Card-Reader")

        val result = getUserAgent(context, SendDiagnostics.Devices, buildVersionProvider)

        assertTrue(result.contains("devices="))
    }

    @Test
    fun userAgentUtil_getUserAgent_includeAllMatchingDevices() {
        val usbDevice2 = mock(UsbDevice::class.java)
        val devices = LinkedHashMap<String, UsbDevice>()
        devices["device1"] = usbDevice
        devices["device2"] = usbDevice2
        mockPackageManagerHandling()
        mockUsbManagerHandling(devices)
        `when`(usbDevice.productName).thenReturn("Some Card Reader")
        `when`(usbDevice.deviceName).thenReturn("/dev/bus/usb/001/001")
        `when`(usbDevice2.productName).thenReturn("Smart Card Reader 2")
        `when`(usbDevice2.deviceName).thenReturn("/dev/bus/usb/001/002")

        val result = getUserAgent(context, SendDiagnostics.Devices, buildVersionProvider)

        assertTrue(result.contains("Some Card Reader"))
        assertTrue(result.contains("Smart Card Reader 2"))
    }

    @Test
    fun userAgentUtil_getUserAgent_includeNfcTrueInNfcMode() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()

        val result = getUserAgent(context, SendDiagnostics.NFC, buildVersionProvider)

        assertEquals(
            "APP riadigidoc/1.2.3.1234 (schema=1; os=Android ${Build.VERSION.RELEASE}; lang=en; devicetype=mobile/$expectedDeviceModel; nfc=true)",
            result,
        )
    }

    @Test
    fun userAgentUtil_getAppInfo_returnBaseStringWithoutAppPrefix() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()

        val result = getAppInfo(context, buildVersionProvider = buildVersionProvider)

        assertEquals(
            "riadigidoc/1.2.3.1234 (schema=1; os=Android ${Build.VERSION.RELEASE}; lang=en; devicetype=mobile/$expectedDeviceModel)",
            result,
        )
    }

    @Test
    fun userAgentUtil_getAppInfo_neverIncludesLibPrefixEvenWhenVersionSet() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()
        UserAgentUtil.setLibdigidocppVersion("4.5.6.40")

        val result = getAppInfo(context, buildVersionProvider = buildVersionProvider)

        // "LIB" and "APP" prefixes must not appear — libdigidocpp adds it itself
        assertFalse(result.contains("LIB "))
        assertFalse(result.startsWith("APP "))
    }

    @Test
    fun userAgentUtil_getAppInfo_includeDevicesInDevicesMode() {
        val devices = HashMap<String, UsbDevice>()
        devices["device1"] = usbDevice
        mockPackageManagerHandling()
        mockUsbManagerHandling(devices)
        `when`(usbDevice.productName).thenReturn("Smart Card Reader 1")

        val result = getAppInfo(context, SendDiagnostics.Devices, buildVersionProvider)

        assertEquals(
            "riadigidoc/1.2.3.1234 (schema=1; os=Android ${Build.VERSION.RELEASE}; lang=en; devicetype=mobile/$expectedDeviceModel; devices=Smart Card Reader 1)",
            result,
        )
    }

    @Test
    fun userAgentUtil_getAppInfo_includeNfcTrueInNfcMode() {
        mockPackageManagerHandling()
        mockUsbManagerHandling()

        val result = getAppInfo(context, SendDiagnostics.NFC, buildVersionProvider)

        assertEquals(
            "riadigidoc/1.2.3.1234 (schema=1; os=Android ${Build.VERSION.RELEASE}; lang=en; devicetype=mobile/$expectedDeviceModel; nfc=true)",
            result,
        )
    }

    private fun mockPackageManagerHandling(sdkInt: Int = Build.VERSION.SDK_INT) {
        `when`(context.packageManager).thenReturn(packageManager)
        `when`(context.packageName).thenReturn("App")
        `when`(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(packageInfo)
        `when`(buildVersionProvider.getSDKInt()).thenReturn(sdkInt)
    }

    private fun mockUsbManagerHandling(devices: HashMap<String, UsbDevice> = hashMapOf()) {
        `when`(context.getSystemService(Context.USB_SERVICE)).thenReturn(usbManager)
        `when`(usbManager.deviceList).thenReturn(devices)
    }
}
