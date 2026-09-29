/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.HrefDynamicText
import ee.ria.DigiDoc.ui.theme.Dimensions.LINE_HEIGHT
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonShadowElevation
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeM
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible
import java.time.LocalDate

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidPinAndCertificateView(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    linkText: String = "",
    linkUrl: String = "",
    isPinBlocked: Boolean = false,
    isPukBlocked: Boolean = false,
    showForgotPin: Boolean = true,
    forgotPinText: String = "",
    onForgotPinClick: (() -> Unit)? = null,
    changePinText: String = "",
    onChangePinClick: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("myEidPinAndCertificateView"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier =
                modifier
                    .fillMaxWidth(),
            shape = RoundedCornerShape(SPadding),
            border = BorderStroke(buttonShadowElevation, MaterialTheme.colorScheme.outlineVariant),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = buttonShadowElevation),
        ) {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_m3_check_circle_48dp_wght400),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        modifier
                            .size(iconSizeM)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .semantics {
                                testTagsAsResourceId = true
                            }.testTag("myEidPinAndCertificateIcon")
                            .notAccessible(),
                )

                Column(
                    modifier =
                        modifier
                            .weight(1f)
                            .focusable()
                            .semantics(mergeDescendants = true) {
                                this.contentDescription = "$title. $subtitle".lowercase()
                                testTagsAsResourceId = true
                            }.testTag("myEidCertificateTitle"),
                ) {
                    Text(
                        modifier = modifier.notAccessible(),
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (linkUrl.isNotBlank()) {
                        HrefDynamicText(
                            modifier = modifier.notAccessible(),
                            text1 = subtitle,
                            text2 = "",
                            linkText = linkText,
                            linkUrl = linkUrl,
                            newLineBeforeLink = true,
                            textStyle =
                                TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    textAlign = TextAlign.Start,
                                    lineHeight = TextUnit(LINE_HEIGHT, TextUnitType.Sp),
                                ),
                        )
                    } else {
                        Text(
                            modifier = modifier.notAccessible(),
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            if (showForgotPin && forgotPinText.isNotBlank() && onForgotPinClick != null) {
                Row(
                    modifier =
                        modifier
                            .padding(SPadding)
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(XSPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        enabled = !isPukBlocked,
                        onClick = onForgotPinClick,
                        modifier =
                            modifier
                                .weight(1f)
                                .semantics {
                                    testTagsAsResourceId = true
                                }.testTag("myEidPinAndCertificateForgotPinButton"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                disabledContentColor = MaterialTheme.colorScheme.outline,
                            ),
                    ) {
                        Text(
                            modifier =
                                modifier
                                    .semantics {
                                        this.contentDescription = forgotPinText.lowercase()
                                        testTagsAsResourceId = true
                                    }.testTag("myEidForgotPinButtonText"),
                            text = forgotPinText,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Button(
                        enabled = !isPinBlocked,
                        onClick = onChangePinClick ?: {},
                        modifier =
                            modifier
                                .weight(1f)
                                .semantics {
                                    testTagsAsResourceId = true
                                }.testTag("myEidPinAndCertificateChangePinButton"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                disabledContentColor = MaterialTheme.colorScheme.outline,
                            ),
                    ) {
                        Text(
                            modifier =
                                modifier
                                    .semantics {
                                        this.contentDescription = changePinText.lowercase()
                                        testTagsAsResourceId = true
                                    }.testTag("myEidPinAndCertificateChangePinButtonText"),
                            text = changePinText,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
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
fun MyEidPinAndCertificateViewPreview() {
    RIADigiDocTheme {
        Column {
            MyEidPinAndCertificateView(
                title = "Identity certificate",
                subtitle = "Certificate is valid until ${LocalDate.now()}",
                forgotPinText = "Forgot PIN?",
                onForgotPinClick = {},
                changePinText = "Change PIN",
            )
            MyEidPinAndCertificateView(
                title = "Identity certificate",
                subtitle = "Certificate is valid until ${LocalDate.now()}",
                forgotPinText = "Forgot PIN?",
                onForgotPinClick = {},
                changePinText = "Change PIN",
            )
        }
    }
}
