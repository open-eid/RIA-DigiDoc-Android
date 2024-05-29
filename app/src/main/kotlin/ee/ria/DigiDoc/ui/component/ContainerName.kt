@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.compose.foundation.focusGroup
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding

@Composable
fun ContainerName(
    modifier: Modifier = Modifier,
    name: String,
    onEditNameClick: () -> Unit = {},
    onSaveContainerClick: () -> Unit = {},
    isContainerSigned: Boolean,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(screenViewLargePadding)
                .focusGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val containerTitle = stringResource(id = R.string.container_title)
        Column(
            modifier =
                modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        this.contentDescription = "$containerTitle ${name.lowercase()}"
                    },
        ) {
            Text(
                text = stringResource(id = R.string.container_title),
            )
            Text(
                text = name,
                modifier =
                    modifier
                        .fillMaxWidth(),
            )
        }

        if (isContainerSigned) {
            IconButton(
                onClick = onSaveContainerClick,
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_save),
                        contentDescription = "${stringResource(
                            id = R.string.document_save_button,
                        )} ${name.lowercase()}",
                        tint = Blue500,
                    )
                },
                modifier =
                    modifier
                        .size(iconSize),
            )
        } else {
            IconButton(
                onClick = onEditNameClick,
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
