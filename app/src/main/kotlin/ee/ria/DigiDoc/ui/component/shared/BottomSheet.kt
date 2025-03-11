@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.domain.model.bottomSheet.BottomSheetButton
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun BottomSheet(
    modifier: Modifier,
    showSheet: Boolean,
    onDismiss: () -> Unit,
    buttons: List<BottomSheetButton>,
) {
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(SPadding)
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("bottomSheetContainer"),
            ) {
                buttons.forEach {
                        (
                            showButton, icon, text, isExtraActionButtonShown,
                            extraActionIcon, contentDescription, action,
                        ),
                    ->
                    if (showButton) {
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        action()
                                        onDismiss()
                                    }
                                    .padding(XSPadding),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(icon),
                                contentDescription = null,
                                modifier =
                                    modifier
                                        .padding(XSPadding)
                                        .size(iconSizeXXS)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("bottomSheetIcon")
                                        .notAccessible(),
                            )
                            Spacer(modifier = modifier.width(XSPadding))
                            Text(
                                modifier =
                                    modifier
                                        .semantics {
                                            this.contentDescription = contentDescription
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("bottomSheetText"),
                                text = text,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            if (isExtraActionButtonShown) {
                                Spacer(modifier = modifier.weight(1f))
                                Icon(
                                    imageVector = ImageVector.vectorResource(extraActionIcon),
                                    contentDescription = null,
                                    modifier =
                                        modifier
                                            .padding(XSPadding)
                                            .size(iconSizeXXS)
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("bottomSheetExtraIcon")
                                            .notAccessible(),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = modifier.height(SPadding))
            }
        }
    }
}
