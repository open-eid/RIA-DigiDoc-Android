@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Green_2_50
import ee.ria.DigiDoc.ui.theme.Green_2_700
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun TagBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Green_2_50,
    contentColor: Color = Green_2_700,
) {
    Box(
        modifier =
            modifier
                .background(color = backgroundColor, shape = RoundedCornerShape(screenViewLargePadding))
                .padding(horizontal = XSPadding),
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Normal,
            style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagBadge_Preview() {
    RIADigiDocTheme {
        TagBadge(text = "TagText")
    }
}
