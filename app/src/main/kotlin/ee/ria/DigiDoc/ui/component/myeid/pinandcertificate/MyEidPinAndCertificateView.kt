@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonShadowElevation
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeM
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible
import java.time.LocalDate

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun MyEidPinAndCertificateView(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
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
                }
                .testTag("myEidPinAndCertificateView"),
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
                            }
                            .testTag("myEidPinAndCertificateIcon")
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
                            }
                            .testTag("myEidCertificateTitle"),
                ) {
                    Text(
                        modifier = modifier.notAccessible(),
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = modifier.notAccessible(),
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (showForgotPin && forgotPinText.isNotBlank() && onForgotPinClick != null) {
                FlowRow(
                    modifier =
                        modifier
                            .padding(SPadding),
                    verticalArrangement = Arrangement.spacedBy(XSPadding),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    OutlinedButton(
                        enabled = !isPukBlocked,
                        onClick = onForgotPinClick,
                        modifier =
                            modifier
                                .align(Alignment.CenterVertically)
                                .weight(1f)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("myEidPinAndCertificateForgotPinButton"),
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
                                    }
                                    .testTag("myEidForgotPinButtonText"),
                            text = forgotPinText,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = modifier.size(XSPadding))

                    Button(
                        enabled = !isPinBlocked && !isPukBlocked,
                        onClick = onChangePinClick ?: {},
                        modifier = modifier.align(Alignment.CenterVertically),
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
                                    }
                                    .testTag("myEidPinAndCertificateChangePinButton"),
                            text = changePinText,
                            style = MaterialTheme.typography.labelLarge,
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
        MyEidPinAndCertificateView(
            title = "Identity certificate",
            subtitle = "Certificate is valid until ${LocalDate.now()}",
            forgotPinText = "Forgot PIN?",
            onForgotPinClick = {},
            changePinText = "Change PIN",
        )
    }
}
