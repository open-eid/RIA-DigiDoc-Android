@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    Initialization.init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context

    private lateinit var dataStore: DataStore

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStore = DataStore(context)
        viewModel = SettingsViewModel(dataStore)
    }

    @Test
    fun settingsViewModel_init_success() {
        val result = viewModel.dataStore.getCountry()

        assertEquals(0, result)
    }
}
