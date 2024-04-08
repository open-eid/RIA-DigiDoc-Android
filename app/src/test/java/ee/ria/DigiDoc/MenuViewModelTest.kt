@file:Suppress("PackageName")

package ee.ria.DigiDoc

import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.domain.repository.SomeRepository
import ee.ria.DigiDoc.utils.MainDispatcherRule
import ee.ria.DigiDoc.viewmodel.MenuViewModel
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MenuViewModelTest {
    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Test
    fun menuViewModel_Initialization_defaultState() {
        val someRepository: SomeRepository = mock()
        val homeViewModel = MenuViewModel(someRepository)
        val state = homeViewModel.someState

        assertTrue(state.value.equals(SomeObject()))
    }

    @Test
    fun menuViewModel_getObjectById_someObjectWithId() {
        val someRepository: SomeRepository = mock()
        val homeViewModel = MenuViewModel(someRepository)
        val id = 5
        whenever(someRepository.getObjectById(id)).thenReturn(
            flow {
                emit(SomeObject(id = id))
            }.flowOn(Dispatchers.IO),
        )
        homeViewModel.getObject(id)

        val state = homeViewModel.someState

        state.value.id?.let { assertTrue(it.equals(id)) }
    }
}
