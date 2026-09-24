// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service.siva

import android.content.Context
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import java.io.File

interface SivaService {
    fun isSivaConfirmationNeeded(
        context: Context,
        files: List<File>,
    ): Boolean

    suspend fun isTimestampedContainer(signedContainer: SignedContainer): Boolean

    suspend fun getTimestampedContainer(
        context: Context,
        parentContainer: SignedContainer,
        isSivaConfirmed: Boolean,
    ): SignedContainer
}
