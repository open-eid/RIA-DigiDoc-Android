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
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import ee.ria.DigiDoc.common.BuildVersionProvider
import ee.ria.DigiDoc.common.BuildVersionProviderImpl
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.util.Locale
import java.util.stream.Collectors

enum class SendDiagnostics {
    Devices,
    NFC,
    None,
}

object UserAgentUtil {
    private val LOG_TAG = javaClass.simpleName
    private val deviceNameFilters = listOf("Smart", "Reader", "Card")
    private var libdigidocppVersion: String? = null

    fun setLibdigidocppVersion(version: String) {
        libdigidocppVersion = version
    }

    fun resetLibdigidocppVersion() {
        libdigidocppVersion = null
    }

    fun getAppInfo(
        context: Context,
        sendDiagnostics: SendDiagnostics = SendDiagnostics.None,
        buildVersionProvider: BuildVersionProvider = BuildVersionProviderImpl(),
    ): String {
        val sb = StringBuilder()

        sb.append("riadigidoc/").append(getAppVersion(context, buildVersionProvider))

        sb.append(" (schema=1")
        sb.append("; os=Android ").append(Build.VERSION.RELEASE)
        sb.append("; lang=").append(Locale.getDefault().language)

        val deviceType = if (isTablet(context)) "tablet" else "mobile"
        val deviceModel =
            sanitizeField(
                Build.MODEL
                    ?.lowercase()
                    ?.replace(" ", "-")
                    .orEmpty(),
            )
        sb.append("; devicetype=$deviceType/$deviceModel")

        if (sendDiagnostics == SendDiagnostics.Devices) {
            val deviceNames =
                getConnectedUsbDevices(context).map { sanitizeField(it.productName ?: it.deviceName) }
            if (deviceNames.isNotEmpty()) {
                sb.append("; devices=").append(deviceNames.joinToString(", "))
            }
        }

        if (sendDiagnostics == SendDiagnostics.NFC) {
            sb.append("; nfc=true")
        }

        sb.append(")")

        return sb.toString()
    }

    fun getUserAgent(
        context: Context,
        sendDiagnostics: SendDiagnostics = SendDiagnostics.None,
        buildVersionProvider: BuildVersionProvider = BuildVersionProviderImpl(),
    ): String {
        val sb = StringBuilder()

        libdigidocppVersion?.let { version ->
            val arch = Build.SUPPORTED_ABIS?.firstOrNull()?.let { normalizeArch(it) } ?: "unknown"
            sb.append("LIB libdigidocpp/").append(version)
            sb.append(" (").append(arch).append(") ")
        }

        sb.append("APP ").append(getAppInfo(context, sendDiagnostics, buildVersionProvider))

        return sb.toString()
    }

    // Remove non-ASCII and delimiters so a field can't break the header or its structure
    private fun sanitizeField(value: String): String =
        value
            .filter { it.code in 0x20..0x7e }
            .replace(Regex("[;()]"), "")
            .trim()

    private fun isTablet(context: Context): Boolean =
        try {
            context.resources.configuration.smallestScreenWidthDp >= 600
        } catch (_: Exception) {
            false
        }

    private fun normalizeArch(abi: String): String =
        when {
            abi.startsWith("arm64") -> "arm64"
            abi.startsWith("armeabi") -> "arm"
            abi.startsWith("x86_64") -> "x86_64"
            abi.startsWith("x86") -> "x86"
            else -> abi
        }

    private fun getConnectedUsbDevices(context: Context): List<UsbDevice> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val devices = usbManager.getDeviceList()

        @Suppress("UNUSED_DESTRUCTURED_PARAMETER_ENTRY")
        val smartDevices =
            devices.entries
                .stream()
                .filter { (_, value): Map.Entry<String, UsbDevice> ->
                    deviceNameFilters
                        .stream()
                        .anyMatch { charSequence: String ->
                            value.productName?.contains(charSequence) == true
                        } ||
                        deviceNameFilters
                            .stream()
                            .anyMatch { charSequence: String ->
                                value.deviceName.contains(charSequence)
                            }
                }.collect(
                    Collectors.toMap<Map.Entry<String, UsbDevice>, String, UsbDevice>(
                        { (key, _) -> key },
                        { (_, value) -> value },
                    ),
                )
        return ArrayList(smartDevices.values)
    }

    private fun getAppVersion(
        context: Context,
        buildVersionProvider: BuildVersionProvider,
    ): String =
        try {
            val info = getPackageInfo(context, buildVersionProvider)
            "${info.versionName}.${info.longVersionCode}"
        } catch (e: PackageManager.NameNotFoundException) {
            errorLog(LOG_TAG, "Failed getting app version from package info", e)
            "unknown"
        }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getPackageInfo(
        context: Context,
        buildVersionProvider: BuildVersionProvider,
    ): PackageInfo =
        if (buildVersionProvider.getSDKInt() >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
        }
}
