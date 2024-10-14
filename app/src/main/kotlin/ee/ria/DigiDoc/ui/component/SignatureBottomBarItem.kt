@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.annotation.StringRes

data class SignatureBottomBarItem(
    @StringRes val label: Int,
    val contentDescription: String = "",
    val isSubButton: Boolean,
    val showButton: Boolean,
    val onClick: () -> Unit = {},
    val testTag: String = "",
)
