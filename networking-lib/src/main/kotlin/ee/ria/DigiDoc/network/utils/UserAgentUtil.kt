@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.text.TextUtils
import ee.ria.DigiDoc.common.BuildVersionProvider
import ee.ria.DigiDoc.common.BuildVersionProviderImpl
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.util.Locale
import java.util.Objects
import java.util.stream.Collectors

object UserAgentUtil {
    private val LOG_TAG = javaClass.simpleName
    private val deviceNameFilters = listOf("Smart", "Reader", "Card")

    fun getUserAgent(
        context: Context?,
        buildVersionProvider: BuildVersionProvider = BuildVersionProviderImpl(),
    ): String {
        val deviceProductNames = ArrayList<String?>()
        val initializingMessage = StringBuilder()
        if (context != null) {
            for (device in getConnectedUsbDevices(context)) {
                deviceProductNames.add(device.productName)
            }
            initializingMessage.append("riadigidoc/").append(getAppVersion(context, buildVersionProvider))
            initializingMessage.append(" (Android ").append(Build.VERSION.RELEASE).append(")")
            initializingMessage.append(" Lang: ").append(Locale.getDefault().language)
            if (deviceProductNames.isNotEmpty()) {
                initializingMessage.append(" Devices: ")
                    .append(TextUtils.join(", ", deviceProductNames))
            }
        }
        return initializingMessage.toString()
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
                            Objects.requireNonNull<String?>(value.productName)
                                .contains(
                                    charSequence,
                                )
                        } ||
                        deviceNameFilters
                            .stream()
                            .anyMatch { charSequence: String ->
                                value.deviceName.contains(
                                    charSequence,
                                )
                            }
                }
                .collect(
                    Collectors.toMap<Map.Entry<String, UsbDevice>, String, UsbDevice>(
                        { (key, value) -> key },
                        { (key, value) -> value },
                    ),
                )
        return ArrayList(smartDevices.values)
    }

    private fun getAppVersion(
        context: Context,
        buildVersionProvider: BuildVersionProvider,
    ): StringBuilder {
        val versionName = StringBuilder()
        try {
            versionName.append(getPackageInfo(context, buildVersionProvider).versionName)
                .append(".")
                .append(getPackageInfo(context, buildVersionProvider).longVersionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            errorLog(LOG_TAG, "Failed getting app version from package info", e)
        }
        return versionName
    }

    @SuppressLint("NewApi")
    @Throws(PackageManager.NameNotFoundException::class)
    private fun getPackageInfo(
        context: Context,
        buildVersionProvider: BuildVersionProvider,
    ): PackageInfo {
        return if (buildVersionProvider.getSDKInt() >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
        }
    }
}
