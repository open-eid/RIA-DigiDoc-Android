@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidPinAndCertificateView(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isPinBlocked: Boolean = false,
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
                    modifier = modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (showForgotPin && forgotPinText.isNotBlank() && onForgotPinClick != null) {
                Row(
                    modifier =
                        modifier
                            .padding(SPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onForgotPinClick,
                        modifier = modifier.weight(1f),
                    ) {
                        Text(
                            text = forgotPinText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = modifier.width(XSPadding))

                    Button(
                        enabled = !isPinBlocked,
                        onClick = onChangePinClick ?: {},
                        modifier = modifier,
                    ) {
                        Text(
                            text = changePinText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
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
