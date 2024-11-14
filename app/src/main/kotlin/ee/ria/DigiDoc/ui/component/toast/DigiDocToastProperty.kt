@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.toast

import androidx.compose.ui.graphics.Color
import ee.ria.DigiDoc.ui.theme.Blue50
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Blue900

interface DigiDocToastProperty {
    fun getBackgroundColor(): Color

    fun getBorderColor(): Color

    fun getTextColor(): Color
}

class Info : DigiDocToastProperty {
    override fun getBackgroundColor(): Color = Blue50

    override fun getBorderColor(): Color = Blue500

    override fun getTextColor(): Color = Blue900
}
