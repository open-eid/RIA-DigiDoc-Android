@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XXSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonCornerRadius
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape

@Composable
fun SignedContainerBottomBar(
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
                .padding(horizontal = MPadding)
                .padding(top = XXSPadding, bottom = MPadding)
                .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        ElevatedButton(
            onClick = onShareButtonClick,
            modifier =
                modifier
                    .shadow(
                        elevation = buttonCornerRadius,
                        shape = RoundedCornerShape(buttonCornerRadius),
                        ambientColor = MaterialTheme.colorScheme.onSurface,
                        spotColor = MaterialTheme.colorScheme.onSurface,
                    )
                    .clip(buttonRoundedCornerShape)
                    .semantics {
                        contentDescription = shareButtonContentDescriptionText
                    },
            colors =
                ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            shape = buttonRoundedCornerShape,
            contentPadding = PaddingValues(SPadding),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(shareButtonIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = modifier.size(iconSizeXXS),
            )
            Spacer(modifier = modifier.width(XSPadding))
            Text(
                modifier = modifier,
                text = stringResource(shareButtonName),
            )
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
            shareButtonIcon = R.drawable.ic_m3_ios_share_48dp_wght400,
            shareButtonName = R.string.share_button,
            shareButtonContentDescription = R.string.share_button_accessibility,
            onShareButtonClick = {},
        )
    }
}
