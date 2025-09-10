@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXL
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.MobileIdViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MobileIdSignatureUpdateContainer(
    modifier: Modifier = Modifier,
    mobileIdViewModel: MobileIdViewModel,
    onError: () -> Unit = {},
) {
    val context = LocalContext.current
    val controlCode = stringResource(id = R.string.challenge_code_text)
    val controlCodeLoadingText = stringResource(id = R.string.control_code_loading)
    var challengeText by remember { mutableStateOf("") }

    LaunchedEffect(mobileIdViewModel.challenge) {
        mobileIdViewModel.challenge.asFlow().collect { challenge ->
            challenge?.let {
                challengeText = challenge
            }
        }
    }

    LaunchedEffect(mobileIdViewModel.errorState) {
        mobileIdViewModel.errorState.asFlow().collect { error ->
            error?.let {
                onError()
            }
        }
    }

    Column(
        modifier =
            modifier
                .padding(SPadding)
                .padding(vertical = LPadding)
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("signatureUpdateMobileIdContainer"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LPadding),
    ) {
        Icon(
            modifier =
                modifier
                    .fillMaxWidth()
                    .size(iconSizeXXL)
                    .notAccessible(),
            imageVector = ImageVector.vectorResource(R.drawable.mobile_id_logo),
            contentDescription = null,
        )

        Text(
            text = controlCode,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            modifier =
                modifier
                    .fillMaxWidth()
                    .notAccessible(),
        )

        Text(
            textAlign = TextAlign.Center,
            text = if (challengeText.isEmpty()) "- - - -" else challengeText,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                modifier
                    .fillMaxWidth()
                    .focusable()
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        if (challengeText.isEmpty()) {
                            this.contentDescription =
                                "$controlCode $controlCodeLoadingText"
                        } else {
                            this.contentDescription =
                                "$controlCode ${formatNumbers(challengeText)}"
                        }
                    }.testTag("signatureUpdateMobileIdChallenge"),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MobileIdSignatureUpdateContainerPreview() {
    val mobileIdViewModel: MobileIdViewModel = hiltViewModel()
    RIADigiDocTheme {
        MobileIdSignatureUpdateContainer(
            mobileIdViewModel = mobileIdViewModel,
        )
    }
}
