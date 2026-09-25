// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository.siva

import android.content.Context
import ee.ria.DigiDoc.domain.service.siva.SivaService
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SivaRepositoryImpl
    @Inject
    constructor(
        private val sivaService: SivaService,
    ) : SivaRepository {
        override fun isSivaConfirmationNeeded(
            context: Context,
            files: List<File>,
        ): Boolean = sivaService.isSivaConfirmationNeeded(context, files)

        override suspend fun isTimestampedContainer(signedContainer: SignedContainer): Boolean =
            sivaService.isTimestampedContainer(signedContainer)

        override suspend fun getTimestampedContainer(
            context: Context,
            parentContainer: SignedContainer,
            isSivaConfirmed: Boolean,
        ): SignedContainer = sivaService.getTimestampedContainer(context, parentContainer, isSivaConfirmed)
    }
