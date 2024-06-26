@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.domain.preferences.DataStore
import javax.inject.Inject

@HiltViewModel
class SharedSettingsViewModel
    @Inject
    constructor(
        val dataStore: DataStore,
    ) : ViewModel()
