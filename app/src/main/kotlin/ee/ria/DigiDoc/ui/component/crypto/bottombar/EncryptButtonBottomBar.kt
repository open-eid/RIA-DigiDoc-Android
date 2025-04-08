@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.bottombar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EncryptButtonBottomBar(
    modifier: Modifier,
    @DrawableRes shareButtonIcon: Int,
    @StringRes shareButtonName: Int,
    @StringRes shareButtonContentDescription: Int,
    onShareButtonClick: () -> Unit,
) {
    val shareButtonContentDescriptionText = stringResource(shareButtonContentDescription)
    Row(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(horizontal = Dimensions.MPadding)
                .padding(top = Dimensions.XXSPadding, bottom = Dimensions.MPadding)
                .navigationBarsPadding()
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signedContainerContainer"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        ElevatedButton(
            onClick = onShareButtonClick,
            modifier =
                modifier
                    .shadow(
                        elevation = Dimensions.MSCornerRadius,
                        shape = RoundedCornerShape(Dimensions.MSCornerRadius),
                        ambientColor = MaterialTheme.colorScheme.onSurface,
                        spotColor = MaterialTheme.colorScheme.onSurface,
                    )
                    .clip(buttonRoundedCornerShape)
                    .semantics {
                        contentDescription = shareButtonContentDescriptionText
                        testTagsAsResourceId = true
                    }
                    .testTag("signedContainerShareButton"),
            colors =
                ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            shape = buttonRoundedCornerShape,
            contentPadding = PaddingValues(Dimensions.SPadding),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(shareButtonIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = modifier.size(Dimensions.iconSizeXXS),
            )
            Spacer(modifier = modifier.width(Dimensions.XSPadding))
            Text(
                modifier = modifier.notAccessible(),
                text = stringResource(shareButtonName),
            )
        }
    }
}
