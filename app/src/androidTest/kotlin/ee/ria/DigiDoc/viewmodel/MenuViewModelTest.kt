@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

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

class MenuViewModelTest {
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

            val menuViewModel = MenuViewModel(mockTtsWrapper)
            val isMenuViewModelTtsInitalizedObserver: Observer<Boolean> = mock()
            menuViewModel.isTtsInitialized.observeForever(isMenuViewModelTtsInitalizedObserver)

            verify(isMenuViewModelTtsInitalizedObserver).onChanged(true)

            menuViewModel.isTtsInitialized.removeObserver(isMenuViewModelTtsInitalizedObserver)
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

            val menuViewModel = MenuViewModel(mockTtsWrapper)
            val isEstonianLanguageUsed = menuViewModel.isEstonianLanguageUsed()

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

            val menuViewModel = MenuViewModel(mockTtsWrapper)
            val isEstonianLanguageUsed = menuViewModel.isEstonianLanguageUsed()

            assertFalse(isEstonianLanguageUsed)
        }
}
