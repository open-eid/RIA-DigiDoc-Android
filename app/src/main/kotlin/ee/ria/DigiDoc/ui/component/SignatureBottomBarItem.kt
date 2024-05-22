@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

data class SignatureBottomBarItem(
    val label: String = "",
    val icon: ImageVector = Icons.Filled.Home,
    val contentDescription: String = "",
    val onClick: () -> Unit = {},
)
