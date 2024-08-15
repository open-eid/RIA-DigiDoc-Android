@file:Suppress("PackageName")

package ee.ria.DigiDoc

import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.manager.ActivityManagerImpl
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ActivityManagerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockComponentActivity: ComponentActivity

    @Mock
    private lateinit var mockObserver: Observer<Boolean>

    private lateinit var activityManager: ActivityManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        activityManager = ActivityManagerImpl()
        doNothing().`when`(mockComponentActivity).recreate()
    }

    @Test
    fun activityManager_setShouldRecreateActivity_successChangingToTrue() {
        activityManager.shouldRecreateActivity.observeForever(mockObserver)

        activityManager.setShouldRecreateActivity(true)

        verify(mockObserver).onChanged(true)
    }

    @Test
    fun activityManager_setShouldRecreateActivity_initialValueFalse() {
        val value = activityManager.shouldRecreateActivity.value
        value?.let { assertFalse(it) }
    }

    @Test
    fun activityManager_recreateActivity_successRecreatingActivity() {
        activityManager.recreateActivity(mockComponentActivity)

        verify(mockComponentActivity, times(1)).recreate()
    }
}
