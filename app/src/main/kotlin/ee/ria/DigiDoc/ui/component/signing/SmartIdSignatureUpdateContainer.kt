@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import ee.ria.DigiDoc.viewmodel.SmartIdViewModel
import kotlinx.coroutines.delay

@Composable
fun SmartIdSignatureUpdateContainer(
    modifier: Modifier = Modifier,
    smartIdViewModel: SmartIdViewModel,
    onCancelButtonClick: () -> Unit = {},
) {
    var loading by remember { mutableStateOf(false) }

    var challengeText by remember { mutableStateOf("") }
    var infoText by remember { mutableStateOf("") }
    val challengeInfoText = stringResource(id = R.string.signature_update_smart_id_info)
    val selectDeviceInfoText = stringResource(id = R.string.signature_update_smart_id_select_device)

    LaunchedEffect(smartIdViewModel.selectDevice) {
        smartIdViewModel.selectDevice.asFlow().collect { selectDevice ->
            selectDevice?.let {
                infoText = ""
                if (selectDevice) {
                    infoText = selectDeviceInfoText
                }
                if (challengeText.isNotEmpty()) {
                    infoText = challengeInfoText
                }
            }
        }
    }

    LaunchedEffect(smartIdViewModel.challenge) {
        smartIdViewModel.challenge.asFlow().collect { challenge ->
            challenge?.let {
                challengeText = challenge
            }
        }
    }

    LaunchedEffect(smartIdViewModel.errorState) {
        smartIdViewModel.errorState.asFlow().collect { error ->
            error?.let {
                onCancelButtonClick()
            }
        }
    }

    Column(
        modifier = modifier.padding(screenViewLargePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.challenge_code_text),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(screenViewLargePadding),
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
                    .padding(screenViewLargePadding),
        )

        Text(
            text = stringResource(id = R.string.signature_update_smart_id_info),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(screenViewLargePadding),
        )

        PrimaryButton(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = screenViewLargePadding),
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
fun SmartIdSignatureUpdateContainerPreview() {
    val smartIdViewModel: SmartIdViewModel = hiltViewModel()
    RIADigiDocTheme {
        SmartIdSignatureUpdateContainer(
            smartIdViewModel = smartIdViewModel,
        )
    }
}

suspend fun loadSmartIdProgress(updateProgress: (Float) -> Unit) {
    for (i in 1..100) {
        updateProgress(i.toFloat() / 100)
        delay(1000)
    }
}
