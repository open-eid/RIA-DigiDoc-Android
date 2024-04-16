@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.domain.repository.SomeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel
    @Inject
    constructor(
        private val someRepository: SomeRepository,
    ) : ViewModel() {
        private val _someState = mutableStateOf<SomeObject>(SomeObject())
        val someState: State<SomeObject> = _someState

        fun getObject(id: Int) {
            viewModelScope.launch {
                someRepository.getObjectById(id).collect { response ->
                    _someState.value = response
                }
            }
        }
    }
