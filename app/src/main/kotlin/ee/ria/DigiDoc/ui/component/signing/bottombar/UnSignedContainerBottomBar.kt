@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.bottombar

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
import androidx.compose.material3.TextButton
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
fun UnSignedContainerBottomBar(
    modifier: Modifier,
    leftButtonText: String,
    @DrawableRes leftButtonIcon: Int,
    leftButtonContentDescription: String,
    onLeftButtonClick: () -> Unit,
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
                }.testTag("unsignedContainerContainer"),
        contentAlignment = Alignment.Center,
    ) {
        FlowRow(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = XSPadding)
                    .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = onLeftButtonClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(leftButtonIcon),
                    contentDescription = "$leftButtonContentDescription $buttonName",
                    modifier =
                        modifier
                            .size(iconSizeXXS)
                            .focusable(false)
                            .testTag("unsignedContainerLeftButton"),
                )
                Spacer(modifier = modifier.width(XSPadding))
                Text(
                    text = leftButtonText,
                    modifier =
                        modifier
                            .semantics {
                                contentDescription = leftButtonContentDescription
                            },
                )
            }

            OutlinedButton(onClick = onRightButtonClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(rightButtonIcon),
                    contentDescription = null,
                    modifier =
                        modifier
                            .size(iconSizeXXS)
                            .focusable(false)
                            .testTag("unsignedContainerRightButton"),
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
fun UnSignedContainerBottomBarPreview() {
    RIADigiDocTheme {
        UnSignedContainerBottomBar(
            modifier = Modifier,
            leftButtonText = stringResource(R.string.documents_add_button),
            leftButtonContentDescription = stringResource(R.string.documents_add_button),
            leftButtonIcon = R.drawable.ic_m3_add_48dp_wght400,
            onLeftButtonClick = {},
            rightButtonText = stringResource(R.string.sign_button),
            rightButtonContentDescription = stringResource(R.string.sign_button),
            rightButtonIcon = R.drawable.ic_icon_signature,
            onRightButtonClick = {},
        )
    }
}
