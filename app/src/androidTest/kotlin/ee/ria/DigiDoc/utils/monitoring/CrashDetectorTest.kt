@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.monitoring

import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class CrashDetectorTest {
    @Mock
    private lateinit var mockTask: Task<Boolean>

    private lateinit var crashDetector: CrashDetector
    private lateinit var mockCrashlytics: FirebaseCrashlytics

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        mockCrashlytics = mock(FirebaseCrashlytics::class.java)

        crashDetector = CrashDetectorImpl(mockCrashlytics)
    }

    @Test
    fun crashDetector_didAppCrashOnPreviousExecution_successWithTrue() {
        `when`(mockCrashlytics.didCrashOnPreviousExecution()).thenReturn(true)

        assertTrue(crashDetector.didAppCrashOnPreviousExecution())
    }

    @Test
    fun crashDetector_didAppCrashOnPreviousExecution_successWithFalse() {
        `when`(mockCrashlytics.didCrashOnPreviousExecution()).thenReturn(false)

        assertFalse(crashDetector.didAppCrashOnPreviousExecution())
    }

    @Test
    fun crashDetector_checkForUnsentReports_successWithTrue() =
        runBlocking {
            `when`(mockTask.isSuccessful).thenReturn(true)
            `when`(mockTask.isComplete).thenReturn(true)
            `when`(mockTask.result).thenReturn(true)

            `when`(mockCrashlytics.checkForUnsentReports()).thenReturn(mockTask)

            val result = crashDetector.checkForUnsentReports()

            assertTrue(result.result)
        }

    @Test
    fun crashDetector_checkForUnsentReports_successWithFalse() =
        runBlocking {
            `when`(mockTask.isSuccessful).thenReturn(true)
            `when`(mockTask.isComplete).thenReturn(true)
            `when`(mockTask.result).thenReturn(false)

            `when`(mockCrashlytics.checkForUnsentReports()).thenReturn(mockTask)

            val result = crashDetector.checkForUnsentReports()

            assertFalse(result.result)
        }

    @Test
    fun crashDetector_checkForUnsentReports_returnFalseWhenExceptionThrown() =
        runBlocking {
            `when`(mockCrashlytics.checkForUnsentReports()).thenThrow(RuntimeException("Test exception"))

            val resultTask = crashDetector.checkForUnsentReports()

            assertFalse(resultTask.result)
        }

    @Test
    fun crashDetector_sendUnsentReports_success() {
        crashDetector.sendUnsentReports()

        verify(mockCrashlytics).sendUnsentReports()
    }

    @Test
    fun crashDetector_deleteUnsentReports_success() {
        crashDetector.deleteUnsentReports()

        verify(mockCrashlytics).deleteUnsentReports()
    }
}
