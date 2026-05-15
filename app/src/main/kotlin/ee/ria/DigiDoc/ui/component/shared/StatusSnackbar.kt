/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.LElevation
import ee.ria.DigiDoc.ui.theme.Dimensions.MSCornerRadius
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.extendedColorScheme
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarMessage
import ee.ria.DigiDoc.utils.snackbar.SnackbarType

@Composable
fun StatusSnackbarHost() {
    val currentMessage by SnackBarManager.currentMessage.collectAsState()
    var lastMessage by remember { mutableStateOf<SnackBarMessage?>(null) }
    if (currentMessage != null) lastMessage = currentMessage

    AnimatedVisibility(
        visible = currentMessage != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        lastMessage?.let { StatusSnackbar(message = it) }
    }
}

@Composable
fun StatusSnackbar(message: SnackBarMessage) {
    val isError = message.type == SnackbarType.ERROR

    val backgroundColor =
        if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.extendedColorScheme.successContainer
        }
    val contentColor =
        if (isError) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.extendedColorScheme.onSuccessContainer
        }

    val iconRes =
        if (isError) {
            R.drawable.ic_m3_error_48dp_wght400
        } else {
            R.drawable.ic_m3_check_48dp_wght400
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MSPadding)
                .padding(bottom = XSPadding)
                .shadow(
                    elevation = LElevation,
                    shape = RoundedCornerShape(MSCornerRadius),
                    ambientColor = Color.Transparent,
                ).clip(RoundedCornerShape(MSCornerRadius))
                .background(color = backgroundColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SPadding, vertical = MSPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSizeXXS),
            )
            Spacer(modifier = Modifier.width(XSPadding))
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
    }
}
