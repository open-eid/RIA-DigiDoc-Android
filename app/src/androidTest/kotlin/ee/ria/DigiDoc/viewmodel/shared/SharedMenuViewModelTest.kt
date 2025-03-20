@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import java.util.Locale

class SharedMenuViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun menuViewModel_init_successTtsInitialized() =
        runTest {
            val mockTtsWrapper: TextToSpeechWrapper = mock()

            `when`(mockTtsWrapper.initializeSuspend()).thenReturn(true)

            val sharedMenuViewModel = SharedMenuViewModel(mockTtsWrapper)
            val isMenuViewModelTtsInitalizedObserver: Observer<Boolean> = mock()
            sharedMenuViewModel.isTtsInitialized.observeForever(isMenuViewModelTtsInitalizedObserver)

            verify(isMenuViewModelTtsInitalizedObserver).onChanged(true)

            sharedMenuViewModel.isTtsInitialized.removeObserver(isMenuViewModelTtsInitalizedObserver)
        }

    @Test
    fun menuViewModel_isEstonianLanguageUsed_returnTrue() =
        runTest {
            val mockTtsWrapper: TextToSpeechWrapper = mock()

            val estonianLocale = Locale("et", "ET")
            val mockTextToSpeech = mock(TextToSpeech::class.java)
            val mockTextToSpeechVoice = mock(Voice::class.java)
            `when`(mockTtsWrapper.initializeSuspend()).thenReturn(true)
            `when`(mockTtsWrapper.getInstance()).thenReturn(mockTextToSpeech)
            `when`(mockTextToSpeech.voice).thenReturn(mockTextToSpeechVoice)
            `when`(mockTextToSpeech.availableLanguages).thenReturn(setOf(estonianLocale))
            `when`(mockTextToSpeechVoice.locale).thenReturn(estonianLocale)

            Locale.setDefault(estonianLocale)

            val sharedMenuViewModel = SharedMenuViewModel(mockTtsWrapper)
            val isEstonianLanguageUsed = sharedMenuViewModel.isEstonianLanguageUsed()

            assertTrue(isEstonianLanguageUsed)
        }

    @Test
    fun menuViewModel_isEstonianLanguageUsed_returnFalse() =
        runTest {
            val mockTtsWrapper: TextToSpeechWrapper = mock()

            val englishLocale = Locale("en", "US")
            val mockTextToSpeech = mock(TextToSpeech::class.java)
            val mockTextToSpeechVoice = mock(Voice::class.java)
            `when`(mockTtsWrapper.initializeSuspend()).thenReturn(true)
            `when`(mockTtsWrapper.getInstance()).thenReturn(mockTextToSpeech)
            `when`(mockTextToSpeech.voice).thenReturn(mockTextToSpeechVoice)
            `when`(mockTextToSpeech.availableLanguages).thenReturn(setOf())
            `when`(mockTextToSpeechVoice.locale).thenReturn(englishLocale)

            Locale.setDefault(englishLocale)

            val sharedMenuViewModel = SharedMenuViewModel(mockTtsWrapper)
            val isEstonianLanguageUsed = sharedMenuViewModel.isEstonianLanguageUsed()

            assertFalse(isEstonianLanguageUsed)
        }
}
