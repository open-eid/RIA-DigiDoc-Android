@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.ui.theme.Dimensions.radioButtonPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SignatureAddRadioButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    onClick: (() -> Unit)?,
    enabled: Boolean = true,
) {
    val radioColor = radioColor(enabled, selected)
    val textColor = textColor(enabled, selected)
    Button(
        modifier = modifier.padding(zeroPadding),
        shape = RectangleShape,
        colors =
            ButtonColors(
                containerColor = radioColor.value,
                contentColor = textColor.value,
                disabledContainerColor = radioColor.value,
                disabledContentColor = textColor.value,
            ),
        contentPadding = PaddingValues(zeroPadding),
        onClick = {
            if (onClick != null) {
                onClick()
            }
        },
    ) {
        Text(
            modifier =
                modifier
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(horizontal = radioButtonPadding, vertical = zeroPadding),
            text = label,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun radioColor(
    enabled: Boolean,
    selected: Boolean,
): State<Color> {
    val radioAnimationDuration = 100
    val target =
        when {
            enabled && selected -> MaterialTheme.colorScheme.primary
            enabled && !selected -> MaterialTheme.colorScheme.background
            !enabled && selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.background
        }

    return if (enabled) {
        animateColorAsState(target, tween(durationMillis = radioAnimationDuration))
    } else {
        rememberUpdatedState(target)
    }
}

@Composable
fun textColor(
    enabled: Boolean,
    selected: Boolean,
): State<Color> {
    val radioAnimationDuration = 100
    val target =
        when {
            enabled && selected -> MaterialTheme.colorScheme.background
            enabled && !selected -> MaterialTheme.colorScheme.primary
            !enabled && selected -> MaterialTheme.colorScheme.background
            else -> MaterialTheme.colorScheme.primary
        }

    return if (enabled) {
        animateColorAsState(target, tween(durationMillis = radioAnimationDuration))
    } else {
        rememberUpdatedState(target)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureAddRadioButtonPreview() {
    RIADigiDocTheme {
        Column {
            SignatureAddRadioButton(
                selected = true,
                onClick = null,
                label = "Test",
            )
            SignatureAddRadioButton(
                selected = false,
                onClick = null,
                label = "Tested",
            )
        }
    }
}
