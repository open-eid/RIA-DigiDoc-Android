@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.SignatureItem
import ee.ria.DigiDoc.ui.component.ContainerFile
import ee.ria.DigiDoc.ui.component.ContainerName
import ee.ria.DigiDoc.ui.component.PrimaryButton
import ee.ria.DigiDoc.ui.component.SignatureComponent
import ee.ria.DigiDoc.ui.theme.Dimensions.dividerHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import java.util.Date

@Composable
fun SigningNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val exampleFileNames = listOf("exampleFile1.txt", "exampleFile2.txt")
    val exampleSignatures =
        listOf(
            SignatureItem("Example Name, 1234567890", "Signature is valid", Date()),
            SignatureItem("Example Name 2, 12309845678", "Signature is valid", Date()),
        )

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
                )
                Column(
                    modifier =
                        modifier
                            .padding(horizontal = screenViewHorizontalPadding, vertical = screenViewVerticalPadding)
                            .verticalScroll(rememberScrollState()),
                ) {
                    ContainerName(name = "someFile.asice")
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
                        // Use "R.string.signing_container_documents_title" when existing container without signatures is open (eg from Recent Documents)
                        Text(
                            stringResource(
                                id = R.string.signing_documents_title,
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
                                )
                            },
                        )
                    }

                    exampleFileNames.forEach { fileName ->
                        ContainerFile(fileName = fileName)
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
                        // Use "R.string.signing_container_documents_title" when existing container without signatures is open (eg from Recent Documents)
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

                    exampleSignatures.forEach { signature ->
                        SignatureComponent(signature = signature)
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
    RIADigiDocTheme {
        SigningNavigation(navController)
    }
}
