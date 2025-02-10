@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.toast

import androidx.compose.ui.graphics.Color
import ee.ria.DigiDoc.ui.theme.LightOnSecondaryContainer
import ee.ria.DigiDoc.ui.theme.LightOutline
import ee.ria.DigiDoc.ui.theme.LightSecondaryContainer

interface DigiDocToastProperty {
    fun getBackgroundColor(): Color

    fun getBorderColor(): Color

    fun getTextColor(): Color
}

class Info : DigiDocToastProperty {
    override fun getBackgroundColor(): Color = LightSecondaryContainer

    override fun getBorderColor(): Color = LightOutline

    override fun getTextColor(): Color = LightOnSecondaryContainer
}
