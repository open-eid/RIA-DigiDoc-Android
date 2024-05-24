@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.signing

import android.content.Context
import android.os.PowerManager

object PowerUtil {
    fun isPowerSavingMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }
}
