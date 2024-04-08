@file:Suppress("PackageName")

package ee.ria.DigiDoc

import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.domain.repository.SomeRepository
import ee.ria.DigiDoc.utils.MainDispatcherRule
import ee.ria.DigiDoc.viewmodel.HomeViewModel
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HomeViewModelTest {
    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Test
    fun homeViewModel_Initialization_defaultListState() {
        val someRepository: SomeRepository = mock()
        val homeViewModel = HomeViewModel(someRepository)
        val listState = homeViewModel.listState

        assertTrue(listState.equals(listOf(SomeObject())))
    }

    @Test
    fun homeViewModel_getAllObjects_listWithSomeObjectWithId() {
        val someRepository: SomeRepository = mock()
        val homeViewModel = HomeViewModel(someRepository)
        whenever(someRepository.getAllObjects()).thenReturn(
            flow {
                emit(listOf(SomeObject(id = 1)))
            }.flowOn(Dispatchers.IO),
        )
        homeViewModel.getListState()

        val listState = homeViewModel.listState

        listState.get(0).id?.let { assertTrue(it.equals(1)) }
    }
}
