@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.toast

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object ToastUtil {
    fun showMessage(
        context: Context,
        @StringRes message: Int,
    ) {
        Toast.makeText(
            context,
            context.getString(message),
            Toast.LENGTH_LONG,
        ).show()
    }
}
