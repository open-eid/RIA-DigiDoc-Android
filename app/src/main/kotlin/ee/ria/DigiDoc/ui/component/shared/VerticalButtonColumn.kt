@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Normal
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

data class VerticalButtonConfig(
    val title: Int,
    val contentDescription: String = "",
    val isEnabled: Boolean = true,
    val isSubButton: Boolean = true,
    val containerColor: Color = Normal,
    val contentColor: Color = Color.Blue,
    val onClick: () -> Unit = {},
    val testTag: String = "",
)

@Composable
fun VerticalButtonColumn(
    modifier: Modifier = Modifier,
    buttonConfigs: List<VerticalButtonConfig> = emptyList(),
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SPadding),
        verticalArrangement = Arrangement.spacedBy(SPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        buttonConfigs.forEach { config ->
            PrimaryButton(
                modifier = modifier.testTag(config.testTag),
                enabled = config.isEnabled,
                isSubButton = config.isSubButton,
                title = config.title,
                contentDescription = config.contentDescription,
                containerColor = config.containerColor,
                contentColor = config.contentColor,
                onClickItem = config.onClick,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VerticalButtonColumnPreview() {
    RIADigiDocTheme {
        VerticalButtonColumn(
            buttonConfigs =
                listOf(
                    VerticalButtonConfig(
                        title = R.string.ok_button,
                    ),
                    VerticalButtonConfig(
                        title = R.string.cancel_button,
                    ),
                ),
        )
    }
}
