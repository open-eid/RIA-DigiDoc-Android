@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.shared

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun notificationPermissionRequester(): suspend () -> Boolean {
    val context = LocalContext.current

    val permissionResult = remember { CompletableDeferred<Boolean>() }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            permissionResult.complete(isGranted)
        }

    return remember {
        suspend {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                if (permissionResult.isCompleted) {
                    permissionResult.completeExceptionally(
                        CancellationException("Permission request already completed"),
                    )
                }

                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                permissionResult.await()
            }
        }
    }
}
