/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
