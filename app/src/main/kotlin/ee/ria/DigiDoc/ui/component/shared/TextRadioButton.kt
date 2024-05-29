@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeLarge
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun TextRadioButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit = {},
    title: String,
    contentDescription: String,
) {
    ConstraintLayout(
        modifier =
            modifier
                .padding(vertical = screenViewLargePadding)
                .wrapContentHeight()
                .fillMaxWidth(),
    ) {
        val (
            radioButton,
            radioButtonText,
        ) = createRefs()
        RadioButton(
            modifier =
                modifier
                    .size(iconSizeLarge)
                    .constrainAs(radioButton) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
            selected = selected,
            onClick = onClick,
        )
        Text(
            modifier =
                modifier
                    .semantics {
                        this.contentDescription = contentDescription
                    }
                    .padding(start = screenViewLargePadding, end = iconSizeLarge)
                    .padding(end = screenViewLargePadding)
                    .constrainAs(radioButtonText) {
                        start.linkTo(radioButton.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
            text = title,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TextRadioButtonPreview() {
    RIADigiDocTheme {
        Column {
            TextRadioButton(selected = true, title = "Option 1".repeat(10), contentDescription = "")
            TextRadioButton(selected = false, title = "Option 2", contentDescription = "")
            TextRadioButton(selected = false, title = "Option 3", contentDescription = "")
            TextRadioButton(selected = false, title = "Option 4", contentDescription = "")
        }
    }
}
