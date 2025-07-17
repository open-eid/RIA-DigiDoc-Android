@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.bottomSheet

import androidx.annotation.DrawableRes
import ee.ria.DigiDoc.R

data class BottomSheetButton(
    val showButton: Boolean = true,
    @param:DrawableRes val icon: Int,
    val text: String,
    val isExtraActionButtonShown: Boolean = false,
    @param:DrawableRes val extraActionIcon: Int = R.drawable.ic_m3_arrow_right_48dp_wght400,
    val contentDescription: String = "",
    val onClick: () -> Unit,
)
