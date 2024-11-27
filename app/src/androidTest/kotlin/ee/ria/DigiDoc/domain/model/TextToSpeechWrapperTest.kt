@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapper
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapperImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class TextToSpeechWrapperTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    lateinit var textToSpeechWrapper: TextToSpeechWrapper

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        textToSpeechWrapper = TextToSpeechWrapperImpl(context)
    }

    @Test
    fun textToSpeechWrapper_initializeSuspend_success() =
        runBlocking {
            val result = textToSpeechWrapper.initializeSuspend()

            assertTrue(result)
        }

    @Test
    fun textToSpeechWrapper_getInstance_success() =
        runBlocking {
            textToSpeechWrapper.initializeSuspend()

            val currentInstance = textToSpeechWrapper.getInstance()

            assertNotNull(currentInstance)
        }

    @Test
    fun textToSpeechWrapper_shutdown_success() =
        runBlocking {
            textToSpeechWrapper.initializeSuspend()

            val currentInstance = textToSpeechWrapper.getInstance()

            assertNotNull(currentInstance)

            textToSpeechWrapper.shutdown()

            val shutdownInstance = textToSpeechWrapper.getInstance()

            assertNull(shutdownInstance)
        }
}
