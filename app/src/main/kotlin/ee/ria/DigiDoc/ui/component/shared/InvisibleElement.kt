@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R.string.last_invisible_element_name
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InvisibleElement(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = last_invisible_element_name),
        modifier =
            modifier
                .height(invisibleElementHeight)
                .alpha(0.01f)
                .focusable(false)
                .semantics {
                    invisibleToUser()
                    testTagsAsResourceId = true
                }
                .testTag("lastInvisibleElement"),
    )
}
