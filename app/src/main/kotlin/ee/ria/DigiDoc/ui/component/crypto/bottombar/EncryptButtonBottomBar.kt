@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.bottombar

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EncryptButtonBottomBar(
    modifier: Modifier,
    @DrawableRes encryptButtonIcon: Int,
    @StringRes encryptButtonName: Int,
    @StringRes encryptButtonContentDescription: Int,
    isEncryptButtonEnabled: Boolean = false,
    onEncryptButtonClick: () -> Unit,
) {
    val buttonName = stringResource(id = R.string.button_name)
    val encryptButtonText = stringResource(encryptButtonName)
    val encryptButtonContentDescriptionText = stringResource(encryptButtonContentDescription)
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
                }.testTag("encryptContainerContainer"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        OutlinedButton(
            enabled = isEncryptButtonEnabled,
            onClick = onEncryptButtonClick,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(encryptButtonIcon),
                contentDescription = null,
                modifier =
                    modifier
                        .size(iconSizeXXS)
                        .focusable(false)
                        .testTag("encryptContainerRightButton"),
            )
            Spacer(modifier = modifier.width(XSPadding))
            Text(
                text = encryptButtonText,
                modifier =
                    modifier
                        .semantics {
                            contentDescription = "$encryptButtonContentDescriptionText $buttonName"
                        },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptButtonBottomBarPreview() {
    RIADigiDocTheme {
        EncryptButtonBottomBar(
            modifier = Modifier,
            encryptButtonName = R.string.encrypt_button,
            encryptButtonContentDescription = R.string.encrypt_button,
            encryptButtonIcon = R.drawable.ic_m3_encrypted_48dp_wght400,
            onEncryptButtonClick = {},
        )
    }
}
