@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    @StringRes text1: Int,
    @StringRes text2: Int,
    @StringRes linkText: Int,
    @StringRes linkUrl: Int,
    modifier: Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = onDismiss,
        ) {
            Surface(
                modifier =
                    modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = screenViewLargePadding),
            ) {
                HrefMessageDialog(
                    modifier = modifier,
                    text1 = text1,
                    text2 = text2,
                    linkText = linkText,
                    linkUrl = linkUrl,
                    showLinkOnOneLine = true,
                    cancelButtonClick = onDismiss,
                    okButtonClick = onConfirm,
                )
            }
        }
    }
}
