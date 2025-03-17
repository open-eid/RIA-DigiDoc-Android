@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.MobileIdViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MobileIdSignatureUpdateContainer(
    modifier: Modifier = Modifier,
    mobileIdViewModel: MobileIdViewModel,
    onCancelButtonClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var challengeText by remember { mutableStateOf("") }
    val challengeInfoText = stringResource(id = R.string.signature_update_mobile_id_info)

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
                onCancelButtonClick()
            }
        }
    }

    LaunchedEffect(challengeText) {
        if (challengeText.isNotEmpty()) {
            AccessibilityUtil.sendAccessibilityEvent(
                context,
                TYPE_ANNOUNCEMENT,
                challengeInfoText,
            )
        }
    }

    Column(
        modifier =
            modifier
                .padding(screenViewLargePadding)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signatureUpdateMobileIdContainer"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.challenge_code_text),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            modifier =
                modifier
                    .wrapContentSize()
                    .padding(screenViewLargePadding)
                    .notAccessible(),
        )
        Text(
            textAlign = TextAlign.Center,
            text = challengeText,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(screenViewLargePadding)
                    .semantics {
                        this.contentDescription =
                            "${context.getString(R.string.challenge_code_text)} $challengeText"
                    }
                    .testTag("signatureUpdateMobileIdChallenge"),
        )

        Text(
            text = challengeInfoText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            modifier =
                modifier
                    .wrapContentSize()
                    .padding(screenViewLargePadding)
                    .testTag("signatureUpdateMobileIdChallengeText"),
        )

        PrimaryButton(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = screenViewLargePadding)
                    .testTag("signatureUpdateMobileIdCancelButton"),
            title = R.string.cancel_button,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Red500,
            onClickItem = onCancelButtonClick,
            isSubButton = true,
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
