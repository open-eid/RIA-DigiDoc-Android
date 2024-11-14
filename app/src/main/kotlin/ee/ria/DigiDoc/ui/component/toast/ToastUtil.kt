@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.toast

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.ui.theme.Dimensions.border
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.toastMinSize
import ee.ria.DigiDoc.ui.theme.Dimensions.toastPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.toastRoundShapeCorner

object ToastUtil {
    fun showMessage(
        context: Context,
        @StringRes message: Int,
    ) {
        Toast.makeText(
            context,
            context.getString(message),
            Toast.LENGTH_LONG,
        ).show()
    }

    @Composable
    fun DigiDocToast(
        message: String,
        duration: Int = Toast.LENGTH_LONG,
        padding: PaddingValues =
            PaddingValues(
                top = screenViewLargePadding,
                start = screenViewSmallPadding,
                end = screenViewSmallPadding,
                bottom = toastPadding,
            ),
        contentAlignment: Alignment = Alignment.BottomCenter,
    ) {
        val digiDocInfoToast = DigiDocToast(LocalContext.current)
        digiDocInfoToast.MakeToast(
            message = message,
            duration = duration,
            type = Info(),
            padding = padding,
            contentAlignment = contentAlignment,
        )
        digiDocInfoToast.show()
    }

    @Composable
    fun SetView(
        modifier: Modifier = Modifier,
        messageTxt: String,
        backgroundColor: Color,
        borderColor: Color,
        textColor: Color,
        padding: PaddingValues,
        contentAlignment: Alignment,
    ) {
        val shape = RoundedCornerShape(toastRoundShapeCorner)
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(padding),
            contentAlignment = contentAlignment,
        ) {
            Surface(
                modifier =
                    modifier
                        .wrapContentSize(),
                color = Color.Transparent,
            ) {
                Row(
                    modifier =
                        modifier
                            .defaultMinSize(minHeight = toastMinSize)
                            .background(
                                color = backgroundColor,
                                shape = shape,
                            )
                            .border(
                                width = border,
                                color = borderColor,
                                shape = shape,
                            )
                            .fillMaxWidth()
                            .padding(screenViewSmallPadding),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = modifier,
                        text = messageTxt,
                        textAlign = TextAlign.Start,
                        color = textColor,
                    )
                }
            }
        }
    }
}
