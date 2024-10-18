@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R.string.last_invisible_element_name
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight

@Composable
fun InvisibleElement(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = last_invisible_element_name),
        modifier =
            modifier
                .height(dividerHeight)
                .alpha(0f)
                .focusable(false)
                .testTag("lastInvisibleElement"),
    )
}
