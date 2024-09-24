@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.utils.monitoring.CrashDetector
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.check
import java.util.concurrent.ExecutionException

class HomeViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataStore: DataStore

    @Mock
    private lateinit var crashDetector: CrashDetector

    @Mock
    private lateinit var mockTask: Task<Boolean>

    private lateinit var mockCrashlytics: FirebaseCrashlytics

    private lateinit var context: Context

    private lateinit var viewModel: HomeViewModel

    @Mock
    private lateinit var hasUnsentReportsObserver: Observer<Task<Boolean>>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        mockCrashlytics = mock(FirebaseCrashlytics::class.java)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStore = DataStore(context)

        viewModel = HomeViewModel(dataStore, crashDetector)
    }

    @Test
    fun homeViewModel_checkForUnsentReports_success() {
        verify(crashDetector).checkForUnsentReports()
    }

    @Test
    fun homeViewModel_checkForUnsentReports_hasUnsentReportsSetSuccessfully() {
        `when`(mockTask.result).thenReturn(true)
        `when`(crashDetector.checkForUnsentReports()).thenReturn(mockTask)

        val homeViewModel = HomeViewModel(dataStore, crashDetector)

        homeViewModel.hasUnsentReports.observeForever(hasUnsentReportsObserver)

        verify(hasUnsentReportsObserver).onChanged(mockTask)
    }

    @Test
    fun homeViewModel_sendUnsentReports_success() =
        runTest {
            `when`(crashDetector.checkForUnsentReports()).thenReturn(mockTask)

            `when`(mockTask.isComplete).thenReturn(true)
            `when`(mockTask.isSuccessful).thenReturn(true)
            `when`(mockTask.result).thenReturn(true)

            val homeViewModel = HomeViewModel(dataStore, crashDetector)

            homeViewModel.hasUnsentReports.observeForever(hasUnsentReportsObserver)

            homeViewModel.sendUnsentReports()

            verify(crashDetector).sendUnsentReports()
        }

    @Test
    fun homeViewModel_deleteUnsentReports_success() {
        viewModel.deleteUnsentReports()

        verify(crashDetector).deleteUnsentReports()
    }

    @Test
    fun homeViewModel_deleteUnsentReports_successWhenNoUnsentReports() =
        runTest {
            `when`(mockTask.isComplete).thenReturn(true)
            `when`(mockTask.isSuccessful).thenReturn(true)
            `when`(mockTask.result).thenReturn(false)
            `when`(crashDetector.checkForUnsentReports()).thenReturn(mockTask)

            viewModel = HomeViewModel(dataStore, crashDetector)

            viewModel.sendUnsentReports()

            verify(crashDetector).deleteUnsentReports()
        }

    @Test
    fun homeViewModel_didAppCrashOnPreviousExecution_success() {
        `when`(crashDetector.didAppCrashOnPreviousExecution()).thenReturn(true)

        val result = viewModel.didAppCrashOnPreviousExecution()

        assertTrue(result)
    }

    @Test
    fun homeViewModel_isCrashSendingAlwaysEnabled_successWithTrue() {
        dataStore.setIsCrashSendingAlwaysEnabled(true)

        val result = viewModel.isCrashSendingAlwaysEnabled()

        assertTrue(result)
    }

    @Test
    fun homeViewModel_isCrashSendingAlwaysEnabled_successWithFalse() {
        dataStore.setIsCrashSendingAlwaysEnabled(false)

        val result = viewModel.isCrashSendingAlwaysEnabled()

        assertFalse(result)
    }

    @Test
    fun homeViewModel_getIsCrashSendingAlwaysEnabled_successWithTrue() {
        viewModel.setCrashSendingAlwaysEnabled(true)

        assertTrue(dataStore.getIsCrashSendingAlwaysEnabled())
    }

    @Test
    fun homeViewModel_getIsCrashSendingAlwaysEnabled_successWithFalse() {
        viewModel.setCrashSendingAlwaysEnabled(false)

        assertFalse(dataStore.getIsCrashSendingAlwaysEnabled())
    }

    @Test
    fun homeViewModel_checkForUnsentReports_returnFalseWhenExceptionThrown() {
        `when`(crashDetector.checkForUnsentReports()).thenThrow(RuntimeException("Test Exception"))

        val homeViewModel = HomeViewModel(dataStore, crashDetector)

        homeViewModel.hasUnsentReports.observeForever(hasUnsentReportsObserver)

        verify(hasUnsentReportsObserver).onChanged(
            check { task ->
                assertTrue(task.isSuccessful)
                assertEquals(false, task.result)
            },
        )
    }

    @Test
    fun homeViewModel_sendUnsentReports_dontSendWhenExceptionThrown() =
        runTest {
            `when`(mockTask.isComplete).thenReturn(true)
            `when`(mockTask.isSuccessful).thenReturn(false)
            `when`(
                mockTask.getResult(Throwable::class.java),
            ).thenThrow(ExecutionException("Test Exception", RuntimeException("Test exception")))

            `when`(crashDetector.checkForUnsentReports()).thenReturn(mockTask)

            val homeViewModel = HomeViewModel(dataStore, crashDetector)

            homeViewModel.hasUnsentReports.observeForever(hasUnsentReportsObserver)

            homeViewModel.sendUnsentReports()

            verify(crashDetector, never()).sendUnsentReports()
            verify(crashDetector, never()).deleteUnsentReports()
        }
}
