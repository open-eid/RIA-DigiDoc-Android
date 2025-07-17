@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechWrapperImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : TextToSpeechWrapper {
        private var textToSpeech: TextToSpeech? = null

        override suspend fun initializeSuspend(): Boolean =
            withContext(IO) {
                suspendCancellableCoroutine { continuation ->
                    textToSpeech =
                        TextToSpeech(context) { status ->
                            val isSuccess = status == TextToSpeech.SUCCESS
                            continuation.resumeWith(Result.success(isSuccess))
                        }
                }
            }

        override fun getInstance(): TextToSpeech? = textToSpeech

        override fun shutdown() {
            textToSpeech?.shutdown()
            textToSpeech = null
        }
    }
