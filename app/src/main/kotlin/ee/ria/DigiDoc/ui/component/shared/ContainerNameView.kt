@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ContainerNameView(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    name: String,
    showLeftActionButton: Boolean,
    showRightActionButton: Boolean,
    @StringRes leftActionButtonName: Int,
    @StringRes rightActionButtonName: Int,
    @StringRes leftActionButtonContentDescription: Int,
    @StringRes rightActionButtonContentDescription: Int,
    onLeftActionButtonClick: () -> Unit = {},
    onRightActionButtonClick: () -> Unit = {},
    onMoreOptionsActionButtonClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val leftActionButtonContentDescriptionText = stringResource(leftActionButtonContentDescription)
    val rightActionButtonContentDescriptionText = stringResource(rightActionButtonContentDescription)

    val containerTitleText = stringResource(R.string.container_title)

    val buttonName = stringResource(id = R.string.button_name)

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SPadding)
                .clickable(enabled = !isTalkBackEnabled(context), onClick = onMoreOptionsActionButtonClick)
                .semantics {
                    this.contentDescription = "$containerTitleText $name"
                    testTagsAsResourceId = true
                }
                .testTag("containerNameContainer"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = buttonRoundedCornerShape,
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = SPadding)
                    .padding(start = SPadding, end = XSPadding),
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
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("containerNameIcon")
                                .notAccessible(),
                    )
                }

                Spacer(modifier = modifier.width(SPadding))

                Column(modifier = modifier.weight(1f)) {
                    Text(
                        modifier = modifier.notAccessible(),
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
                                    this.contentDescription = "$containerTitleText $name"
                                    testTagsAsResourceId = true
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
                        modifier =
                            modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("containerNameMoreOptionsIcon"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_more_vert),
                        contentDescription = "${stringResource(R.string.more_options)} $buttonName",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (showLeftActionButton || showRightActionButton) {
                Spacer(modifier = modifier.height(SPadding))

                FlowRow(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (showLeftActionButton) {
                        TextButton(onClick = onLeftActionButtonClick) {
                            Text(
                                modifier =
                                    modifier
                                        .semantics {
                                            contentDescription =
                                                "$leftActionButtonContentDescriptionText $buttonName"
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("containerNameLeftActionButton"),
                                text = stringResource(leftActionButtonName),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (showRightActionButton && rightActionButtonName != 0) {
                        TextButton(onClick = onRightActionButtonClick) {
                            Text(
                                modifier =
                                    modifier
                                        .semantics {
                                            contentDescription =
                                                "$rightActionButtonContentDescriptionText $buttonName"
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("containerNameRightActionButton"),
                                text = stringResource(rightActionButtonName),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
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
            showLeftActionButton = true,
            showRightActionButton = true,
            leftActionButtonName = R.string.signature_update_signature_add,
            rightActionButtonName = R.string.encrypt_button,
            leftActionButtonContentDescription = R.string.signature_update_signature_add,
            rightActionButtonContentDescription = R.string.encrypt_button_accessibility,
            onLeftActionButtonClick = {},
            onRightActionButtonClick = {},
            onMoreOptionsActionButtonClick = {},
        )
    }
}
