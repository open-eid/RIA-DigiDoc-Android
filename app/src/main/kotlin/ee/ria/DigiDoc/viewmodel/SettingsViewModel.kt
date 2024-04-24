@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.SomeRepository
import ee.ria.DigiDoc.utils.flattenToList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val someRepository: SomeRepository,
        val dataStore: DataStore,
    ) : ViewModel() {
        var listState: List<SomeObject> = listOf(SomeObject())

        fun getListState() {
            viewModelScope.launch {
                listState = someRepository.getAllObjects().flattenToList()
            }
        }
    }
