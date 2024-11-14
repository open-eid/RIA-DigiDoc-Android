@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.toast

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class DigiDocToast(context: Context) : Toast(context) {
    @Composable
    fun MakeToast(
        message: String,
        duration: Int = LENGTH_LONG,
        type: DigiDocToastProperty,
        padding: PaddingValues,
        contentAlignment: Alignment,
        colorText: Color = type.getTextColor(),
    ) {
        val context = LocalContext.current
        val views = ComposeView(context)

        views.setContent {
            ToastUtil.SetView(
                messageTxt = message,
                backgroundColor = type.getBackgroundColor(),
                borderColor = type.getBorderColor(),
                textColor = colorText,
                padding = padding,
                contentAlignment = contentAlignment,
            )
        }

        views.setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
        views.setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
        views.setViewTreeViewModelStoreOwner(LocalViewModelStoreOwner.current)

        this.duration = duration
        this.view = views
    }
}
