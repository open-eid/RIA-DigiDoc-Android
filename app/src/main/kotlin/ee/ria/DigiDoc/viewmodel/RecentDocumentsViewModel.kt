@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecentDocumentsViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        suspend fun openDocument(document: File): SignedContainer {
            return SignedContainer.openOrCreate(context, document, listOf(document))
        }

        fun getRecentDocumentList(): List<File> {
            val containerFiles = ContainerUtil.findSignatureContainerFiles(context)

            return containerFiles
        }
    }
