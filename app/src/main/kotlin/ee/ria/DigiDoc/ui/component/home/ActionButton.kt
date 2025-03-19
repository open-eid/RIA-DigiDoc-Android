@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.home

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.MiddleEllipsizeMultilineText
import ee.ria.DigiDoc.ui.theme.Dimensions.MSCornerRadius
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonShadowElevation
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    onClickItem: () -> Unit = {},
    @DrawableRes icon: Int,
    @StringRes title: Int,
    description: String,
    contentDescription: String,
    testTag: String = "",
) {
    val titleText = stringResource(id = title)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = XSPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                modifier
                    .matchParentSize()
                    .offset(y = buttonShadowElevation)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(MSCornerRadius),
                    ),
        )
        Card(
            shape = buttonRoundedCornerShape,
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            elevation =
                CardDefaults.elevatedCardElevation(
                    defaultElevation = buttonShadowElevation,
                ),
            modifier = modifier.padding(bottom = zeroPadding),
        ) {
            Button(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag(testTag),
                shape = buttonRoundedCornerShape,
                onClick = onClickItem,
                colors =
                    ButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                    ),
                contentPadding = PaddingValues(XSPadding),
            ) {
                ConstraintLayout(
                    modifier =
                        modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .align(Alignment.CenterVertically),
                ) {
                    val (
                        actionButtonIcon,
                        actionButtonColumn,
                    ) = createRefs()
                    Box(
                        modifier =
                            modifier
                                .constrainAs(actionButtonIcon) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                }
                                .wrapContentHeight()
                                .padding(XSPadding),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .wrapContentHeight()
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                    ),
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
                    }
                    Column(
                        modifier =
                            modifier
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .fillMaxWidth()
                                .padding(end = iconSizeXXS + XSPadding * 5)
                                .constrainAs(actionButtonColumn) {
                                    start.linkTo(actionButtonIcon.end)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                },
                    ) {
                        Text(
                            text = titleText,
                            modifier =
                                modifier
                                    .padding(bottom = XSPadding / 2)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .semantics {
                                        this.contentDescription = contentDescription
                                    },
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Start,
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
                                    .testTag("actionButtonDescription"),
                            text = description,
                            maxLines = 4,
                            textColor = MaterialTheme.colorScheme.onSurface.toArgb(),
                            textStyle = MaterialTheme.typography.bodyMedium,
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
fun SettingsItemPreview() {
    RIADigiDocTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ActionButton(
                    title = R.string.main_home_crypto_title,
                    description = stringResource(R.string.main_home_crypto_description),
                    contentDescription = " ",
                    icon = R.drawable.ic_m3_encrypted_48dp_wght400,
                )
                ActionButton(
                    title = R.string.main_home_open_document_title,
                    description = stringResource(R.string.main_home_open_document_description),
                    contentDescription = " ",
                    icon = R.drawable.ic_m3_attach_file_48dp_wght400,
                )
            }
        }
    }
}
