@file:Suppress("PackageName")

package ee.ria.DigiDoc.common

import android.os.Build

class BuildVersionProviderImpl : BuildVersionProvider {
    override fun getSDKInt(): Int = Build.VERSION.SDK_INT
}
