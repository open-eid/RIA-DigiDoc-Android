@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.bottombar

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ToEncryptContainerBottomBar(
    modifier: Modifier,
    rightButtonText: String,
    rightButtonContentDescription: String,
    @DrawableRes rightButtonIcon: Int,
    onRightButtonClick: () -> Unit,
) {
    val buttonName = stringResource(id = R.string.button_name)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(start = XSPadding, end = MPadding)
                .padding(vertical = XSPadding)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("encryptContainerBottomBar"),
        contentAlignment = Alignment.Center,
    ) {
        FlowRow(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = XSPadding)
                    .navigationBarsPadding(),
            horizontalArrangement = Arrangement.End,
            verticalArrangement = Arrangement.Center,
        ) {
            OutlinedButton(onClick = onRightButtonClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(rightButtonIcon),
                    contentDescription = null,
                    modifier =
                        modifier
                            .size(iconSizeXXS)
                            .focusable(false)
                            .testTag("encryptContainerRightButton"),
                )
                Spacer(modifier = modifier.width(XSPadding))
                Text(
                    text = rightButtonText,
                    modifier =
                        modifier
                            .semantics {
                                contentDescription = "$rightButtonContentDescription $buttonName"
                            },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptedContainerBottomBarPreview() {
    RIADigiDocTheme {
        ToEncryptContainerBottomBar(
            modifier = Modifier,
            rightButtonText = stringResource(R.string.next_button),
            rightButtonContentDescription = stringResource(R.string.next_button),
            rightButtonIcon = R.drawable.ic_m3_arrow_forward_48dp_wght400,
            onRightButtonClick = {},
        )
    }
}
