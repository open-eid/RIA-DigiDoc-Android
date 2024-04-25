@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import javax.inject.Inject

@HiltViewModel
class SigningViewModel
    @Inject
    constructor() : ViewModel() {
        var shouldResetSignedContainer = mutableStateOf(false)

        fun handleBackButton(navController: NavController) {
            navController.popBackStack(navController.graph.findStartDestination().id, false)
            shouldResetSignedContainer.value = true
        }

        fun isExistingContainerNoSignatures(signedContainer: SignedContainer?): Boolean {
            return isContainerWithoutSignatures(signedContainer) &&
                signedContainer?.isExistingContainer() == true
        }

        fun isExistingContainer(signedContainer: SignedContainer?): Boolean {
            return signedContainer?.isExistingContainer() == true
        }

        fun isContainerWithoutSignatures(signedContainer: SignedContainer?): Boolean {
            return signedContainer?.getSignatures()?.isEmpty() == true
        }
    }
