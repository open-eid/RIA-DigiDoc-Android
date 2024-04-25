@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.ContainerFile
import ee.ria.DigiDoc.ui.component.ContainerName
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.SigningViewModel

@Composable
fun SigningNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedContainerViewModel: SharedContainerViewModel,
    signingViewModel: SigningViewModel = hiltViewModel(),
) {
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val shouldResetContainer by signingViewModel.shouldResetSignedContainer

    BackHandler {
        signingViewModel.handleBackButton(navController)
    }

    DisposableEffect(shouldResetContainer) {
        onDispose {
            if (shouldResetContainer) {
                sharedContainerViewModel.resetSignedContainer()
            }
        }
    }

    Scaffold(
        bottomBar = {
            SigningBottomBar(modifier = modifier)
        },
    ) { innerPadding ->
        Surface(
            modifier =
            modifier
                .fillMaxSize()
                .padding(innerPadding)
                .focusGroup(),
        ) {
            Column {
                // Added top bar here instead of Scaffold -> topBar
                // To better support keyboard navigation
                SigningTopBar(
                    navController,
                    modifier = modifier,
                    onBackButtonClick = {
                        signingViewModel.handleBackButton(navController)
                    },
                )
                Column(
                    modifier =
                    modifier
                        .padding(
                            horizontal = screenViewHorizontalPadding,
                            vertical = screenViewVerticalPadding,
                        )
                        .verticalScroll(rememberScrollState()),
                ) {
                    ContainerName(
                        name = signedContainer?.getName() ?: "",
                        isContainerSigned = signedContainer?.getSignatures()?.isNotEmpty() == true,
                    )

                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = screenViewHorizontalPadding,
                                    vertical = screenViewVerticalPadding,
                                ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val containerDocumentsTitle =
                            if (signingViewModel.isExistingContainerNoSignatures(signedContainer)) {
                                R.string.signing_container_documents_title
                            } else {
                                R.string.signing_documents_title
                            }
                        Text(
                            stringResource(
                                id = containerDocumentsTitle,
                            ),
                            modifier =
                                modifier
                                    .weight(1f)
                                    .semantics { heading() },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (signingViewModel.isExistingContainerNoSignatures(signedContainer)) {
                            IconButton(
                                onClick = {},
                                modifier = modifier.size(iconSize),
                                content = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_add_circle),
                                        contentDescription =
                                            stringResource(
                                                id = R.string.documents_add_button_accessibility,
                                            ).lowercase(),
                                        tint = Blue500,
                                    )
                                },
                            )
                        }
                    }

                    signedContainer?.getDataFiles()?.forEach { dataFile ->
                        ContainerFile(dataFile = dataFile)
                    }

                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = screenViewHorizontalPadding,
                                    vertical = screenViewVerticalPadding,
                                ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PrimaryButton(
                            title = R.string.documents_add_button,
                            contentDescription = stringResource(id = R.string.documents_add_button_accessibility),
                        )
                    }

                    if (signingViewModel.isExistingContainer(signedContainer)) {
                        HorizontalDivider(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenViewHorizontalPadding)
                                    .height(dividerHeight),
                        )

                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = screenViewHorizontalPadding,
                                        vertical = screenViewVerticalPadding,
                                    ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(
                                    id = R.string.signing_container_signatures_title,
                                ),
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics { heading() },
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        if (signingViewModel.isContainerWithoutSignatures(signedContainer)) {
                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = screenViewHorizontalPadding,
                                            vertical = screenViewVerticalPadding,
                                        ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(
                                        id = R.string.signing_container_signatures_empty,
                                    ),
                                    modifier = modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        signedContainer?.getSignatures()?.forEach { signature ->
                            SignatureComponent(signature = signature)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SigningNavigationPreview() {
    val navController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        SigningNavigation(navController, sharedContainerViewModel = sharedContainerViewModel)
    }
}
