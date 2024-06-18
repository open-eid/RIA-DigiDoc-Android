@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun RadioButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    @StringRes label: Int,
    contentDescription: String,
    onClick: (() -> Unit)?,
    enabled: Boolean = true,
) {
    val radioColor = radioColor(enabled, selected)
    val textColor = textColor(enabled, selected)

    PrimaryButton(
        modifier = modifier,
        title = label,
        contentDescription =
            if (selected) {
                "$contentDescription ${stringResource(id = R.string.signature_method_selected)}"
            } else {
                "${stringResource(id = R.string.signature_method)} $contentDescription"
            },
        isSubButton = !selected,
        containerColor = radioColor.value,
        contentColor = textColor.value,
        onClickItem = {
            if (onClick != null) {
                onClick()
            }
        },
    )
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
            RadioButton(
                selected = true,
                onClick = null,
                label = R.string.signature_update_signature_add_method_mobile_id,
                contentDescription = "Mobile-ID",
            )
            RadioButton(
                selected = false,
                onClick = null,
                label = R.string.signature_update_signature_add_method_smart_id,
                contentDescription = "Smart-ID",
            )
        }
    }
}
