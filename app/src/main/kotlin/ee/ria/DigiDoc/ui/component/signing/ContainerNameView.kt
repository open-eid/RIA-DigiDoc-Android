@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.MiddleEllipsizeMultilineText
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape

@Composable
fun ContainerNameView(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    name: String,
    showActionButtons: Boolean,
    @StringRes leftActionButtonName: Int,
    @StringRes rightActionButtonName: Int,
    @StringRes leftActionButtonContentDescription: Int,
    @StringRes rightActionButtonContentDescription: Int,
    onLeftActionButtonClick: () -> Unit = {},
    onRightActionButtonClick: () -> Unit = {},
    onMoreOptionsActionButtonClick: () -> Unit = {},
) {
    val leftActionButtonContentDescriptionText = stringResource(leftActionButtonContentDescription)
    val rightActionButtonContentDescriptionText = stringResource(rightActionButtonContentDescription)
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SPadding)
                .clickable(enabled = true, onClick = onMoreOptionsActionButtonClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = buttonRoundedCornerShape,
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = SPadding)
                    .padding(top = SPadding, bottom = XSPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        modifier
                            .wrapContentHeight()
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier =
                            modifier
                                .padding(XSPadding)
                                .size(iconSizeXXS)
                                .wrapContentHeight(align = Alignment.CenterVertically),
                    )
                }

                Spacer(modifier = modifier.width(SPadding))

                Column(modifier = modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.signature_update_name_update_name),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    MiddleEllipsizeMultilineText(
                        modifier =
                            modifier
                                .padding(zeroPadding)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .focusable(false)
                                .semantics {
                                    this.contentDescription = contentDescription
                                }
                                .testTag("signedContainerName"),
                        text = name,
                        maxLines = 4,
                        textColor = MaterialTheme.colorScheme.onSurface.toArgb(),
                        textStyle =
                            TextStyle(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }

                IconButton(onClick = onMoreOptionsActionButtonClick) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_options),
                    )
                }
            }

            if (showActionButtons) {
                Spacer(modifier = modifier.height(SPadding))

                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onLeftActionButtonClick) {
                        Text(
                            modifier =
                                modifier
                                    .semantics {
                                        contentDescription = leftActionButtonContentDescriptionText
                                    },
                            text = stringResource(leftActionButtonName),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    TextButton(onClick = onRightActionButtonClick) {
                        Text(
                            modifier =
                                modifier
                                    .semantics {
                                        contentDescription = rightActionButtonContentDescriptionText
                                    },
                            text = stringResource(rightActionButtonName),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignedContainerNameViewPreview() {
    RIADigiDocTheme {
        ContainerNameView(
            icon = R.drawable.ic_m3_stylus_note_48dp_wght400,
            name = "Signed container.asice",
            showActionButtons = true,
            leftActionButtonName = R.string.signature_update_signature_add,
            rightActionButtonName = R.string.crypto_button,
            leftActionButtonContentDescription = R.string.signature_update_signature_add,
            rightActionButtonContentDescription = R.string.crypto_button_accessibility,
            onLeftActionButtonClick = {},
            onRightActionButtonClick = {},
            onMoreOptionsActionButtonClick = {},
        )
    }
}
