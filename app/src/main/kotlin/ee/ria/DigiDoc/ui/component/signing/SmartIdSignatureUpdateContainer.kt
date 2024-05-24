@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.SmartIdViewModel
import kotlinx.coroutines.delay

@Composable
fun SmartIdSignatureUpdateContainer(
    modifier: Modifier = Modifier,
    smartIdViewModel: SmartIdViewModel,
    onCancelButtonClick: () -> Unit = {},
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }

    var challengeText by remember { mutableStateOf("") }
    var infoText by remember { mutableStateOf("") }
    val challengeInfoText = stringResource(id = R.string.signature_update_smart_id_info)
    val selectDeviceInfoText = stringResource(id = R.string.signature_update_smart_id_select_device)
    LaunchedEffect(loading) {
        loadSmartIdProgress { progress ->
            currentProgress = progress
        }
        loading = false
    }

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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = challengeText,
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = Dimensions.textVerticalPadding),
        )
        LinearProgressIndicator(
            progress = { currentProgress },
            modifier = modifier.fillMaxWidth(),
        )
        Text(
            text = infoText,
            style = MaterialTheme.typography.titleSmall,
        )

        Button(
            modifier = modifier.padding(top = Dimensions.textVerticalPadding),
            shape = RectangleShape,
            colors =
                ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.tertiary,
                ),
            onClick = onCancelButtonClick,
        ) {
            Text(
                modifier =
                    modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .semantics {
                            this.contentDescription = ""
                        },
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.cancel_button),
            )
        }
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
