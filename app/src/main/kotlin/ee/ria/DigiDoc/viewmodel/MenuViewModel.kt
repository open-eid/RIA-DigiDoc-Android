@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.SignatureRequest.MAXIMUM_INITIALIZATION_COUNT
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MenuViewModel
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
        private var textToSpeech: TextToSpeech,
    ) : ViewModel() {
        private var initializationCount = 0
        private val _helpViewContentDescription = MutableLiveData("")
        val helpViewContentDescription: LiveData<String> = _helpViewContentDescription

        private val _context = MutableLiveData(appContext)
        val context: LiveData<Context> = _context

        fun setContext(context: Context) {
            _context.postValue(context)
        }

        private fun setHelpViewContentDescription(helpViewContentDescription: String) {
            _helpViewContentDescription.postValue(helpViewContentDescription)
        }

        // Estonian TalkBack does not pronounce "dot"
        private val textToSpeechListener =
            OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val textToSpeechVoice = textToSpeech.voice
                    var language = Locale.getDefault().language
                    val isESTLanguageAvailable: Boolean =
                        isTextToSpeechLanguageAvailable(
                            textToSpeech.availableLanguages,
                            setOf(Locale("est", "EST"), Locale("et", "ET")),
                        )
                    if (textToSpeechVoice != null) {
                        val textToSpeechLocale = textToSpeechVoice.locale
                        if (textToSpeechLocale != null) {
                            val textToSpeechLanguage = textToSpeechLocale.language
                            if (isESTLanguageAvailable ||
                                (textToSpeechLanguage == "et" || textToSpeechLanguage == "est")
                            ) {
                                language = "et"
                            }
                        }
                    }
                    if (language == "et") {
                        setHelpViewContentDescription(
                            (context.value?.getString(R.string.main_home_menu_help) ?: "") +
                                " link " +
                                "w w w punkt i d punkt e e",
                        )
                    } else {
                        setHelpViewContentDescription(
                            (
                                (context.value?.getString(R.string.main_home_menu_help) ?: "") + " " +
                                    TextUtil.splitTextAndJoin(
                                        (context.value?.getString(R.string.main_home_menu_help_url_short) ?: ""),
                                        "",
                                        " ",
                                    )
                            ),
                        )
                    }
                } else {
                    retryInitialization()
                }
            }

        init {
            retryInitialization()
        }

        private fun retryInitialization() {
            if (initializationCount < MAXIMUM_INITIALIZATION_COUNT) {
                initializationCount++
                textToSpeech.shutdown()
                textToSpeech = TextToSpeech(context.value, textToSpeechListener)
            }
        }

        private fun isTextToSpeechLanguageAvailable(
            availableLocales: Set<Locale>?,
            locales: Set<Locale>,
        ): Boolean {
            return if (availableLocales != null) {
                locales.stream().anyMatch { lo: Locale ->
                    availableLocales.stream().anyMatch { al: Locale -> al.language == lo.language }
                }
            } else {
                false
            }
        }
    }
