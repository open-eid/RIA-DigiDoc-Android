@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.Dimensions.textVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun IdCardView(
    modifier: Modifier = Modifier,
    cancelButtonClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.signature_update_id_card_progress_message_initial),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
            textAlign = TextAlign.Center,
        )
        var loading by remember { mutableStateOf(true) }

        if (!loading) return

        CircularProgressIndicator(
            modifier = modifier.size(loadingBarSize),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                modifier = modifier.weight(1f),
                shape = RectangleShape,
                colors =
                    ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.tertiary,
                    ),
                onClick = cancelButtonClick,
            ) {
                Text(
                    modifier =
                        modifier
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .semantics {
                                this.contentDescription = contentDescription
                            },
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.cancel_button),
                )
            }
            Button(
                modifier = modifier.weight(1f),
                shape = RectangleShape,
                enabled = false,
                colors =
                    ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.tertiary,
                    ),
                onClick = { /* TODO */ },
            ) {
                Text(
                    modifier =
                        modifier
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .semantics {
                                this.contentDescription = contentDescription
                            },
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.sign_button),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IdCardViewPreview() {
    RIADigiDocTheme {
        IdCardView()
    }
}
