@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonCornerRadius
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SignedContainerBottomBar(
    modifier: Modifier,
    @DrawableRes leftButtonIcon: Int,
    leftButtonContentDescription: String,
    onLeftButtonClick: () -> Unit,
    @DrawableRes secondLeftButtonIcon: Int,
    secondLeftButtonContentDescription: String,
    onSecondLeftButtonClick: () -> Unit,
    rightButtonContentDescription: String,
    @DrawableRes rightButtonIcon: Int,
    onRightButtonClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(start = XSPadding, end = MPadding)
                .padding(top = XSPadding, bottom = SPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(SPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row {
                IconButton(
                    modifier = modifier.testTag("signedContainerBottomBarLeftIconButton"),
                    onClick = onLeftButtonClick,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = leftButtonIcon),
                        contentDescription = leftButtonContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .focusable(false)
                                .testTag("signedContainerBottomBarLeftIcon"),
                    )
                }

                IconButton(
                    modifier = modifier.testTag("signedContainerBottomBarSecondLeftIconButton"),
                    onClick = onSecondLeftButtonClick,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = secondLeftButtonIcon),
                        contentDescription = secondLeftButtonContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .focusable(false)
                                .testTag("signedContainerBottomBarSecondLeftIcon"),
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(buttonCornerRadius))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .testTag("signedContainerRightButtonContainer"),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    modifier = modifier.testTag("signedContainerBottomBarRightIconButton"),
                    onClick = onRightButtonClick,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = rightButtonIcon),
                        contentDescription = rightButtonContentDescription,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .focusable(false)
                                .testTag("signedContainerBottomBarRightIcon"),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignedContainerBottomBarPreview() {
    RIADigiDocTheme {
        SignedContainerBottomBar(
            modifier = Modifier,
            leftButtonContentDescription = stringResource(R.string.documents_add_button),
            leftButtonIcon = R.drawable.ic_m3_more_vert_48dp_wght400,
            onLeftButtonClick = {},
            secondLeftButtonContentDescription = stringResource(R.string.documents_add_button),
            secondLeftButtonIcon = R.drawable.ic_m3_download_48dp_wght400,
            onSecondLeftButtonClick = {},
            rightButtonContentDescription = stringResource(R.string.sign_button),
            rightButtonIcon = R.drawable.ic_m3_ios_share_48dp_wght400,
            onRightButtonClick = {},
        )
    }
}
