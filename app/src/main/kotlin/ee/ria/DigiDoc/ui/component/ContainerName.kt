@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding

@Composable
fun ContainerName(
    modifier: Modifier = Modifier,
    name: String,
    isContainerSigned: Boolean,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = screenViewHorizontalPadding)
                .focusGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = stringResource(id = R.string.container_title),
                modifier = modifier.focusable(),
            )
            Text(
                text = name,
                modifier =
                    modifier
                        .fillMaxWidth()
                        .focusable(),
            )
        }

        if (isContainerSigned) {
            IconButton(
                onClick = { /* TODO */ },
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_save),
                        contentDescription = stringResource(id = R.string.document_save_button),
                        tint = Blue500,
                    )
                },
                modifier =
                    modifier
                        .size(iconSize),
            )
        } else {
            IconButton(
                onClick = { /* TODO */ },
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_edit),
                        contentDescription = stringResource(id = R.string.signing_container_name_update_button),
                        tint = Blue500,
                    )
                },
                modifier =
                    modifier
                        .size(iconSize),
            )
        }
    }
}
