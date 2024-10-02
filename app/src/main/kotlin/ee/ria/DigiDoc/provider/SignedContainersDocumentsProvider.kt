@file:Suppress("PackageName")

package ee.ria.DigiDoc.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.DIR_SIGNATURE_CONTAINERS
import ee.ria.DigiDoc.common.Constant.SIGNATURE_CONTAINER_MIMETYPES
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class SignedContainersDocumentsProvider : DocumentsProvider() {
    companion object {
        private const val LOG_TAG = "SignedContainersDocumentsProvider"
        private const val ROOT_ID = "root"
    }

    private lateinit var signedDocumentsFolder: File

    override fun onCreate(): Boolean {
        if (context == null) {
            return false
        }
        signedDocumentsFolder =
            File(context?.filesDir, DIR_SIGNATURE_CONTAINERS).apply {
                if (!exists()) mkdirs()
            }
        return true
    }

    override fun queryRoots(projection: Array<out String>?): Cursor =
        MatrixCursor(resolveRootProjection(projection)).apply {
            newRow().apply {
                add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_ID)
                add(DocumentsContract.Root.COLUMN_TITLE, context?.getString(R.string.app_name) ?: "RIA DigiDoc")
                add(DocumentsContract.Root.COLUMN_FLAGS, 0)
                add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)
                add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_ID)
            }
        }

    override fun queryDocument(
        documentId: String?,
        projection: Array<out String>?,
    ): Cursor =
        runCatching {
            MatrixCursor(resolveDocumentProjection(projection)).apply {
                includeFile(this, getFileForDocId(documentId))
            }
        }.getOrElse { e ->
            errorLog(LOG_TAG, e.localizedMessage ?: "Unable to open document: $documentId", e)
            CoroutineScope(Main).launch {
                context?.let { ToastUtil.showMessage(it, R.string.container_load_error) }
            }
            MatrixCursor(projection ?: emptyArray())
        }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?,
    ): Cursor =
        runCatching {
            MatrixCursor(resolveDocumentProjection(projection)).apply {
                val parent = getFileForDocId(parentDocumentId)
                parent.listFiles()?.forEach { file ->
                    context?.let {
                        if (file.mimeType(it) in SIGNATURE_CONTAINER_MIMETYPES) {
                            includeFile(this, file)
                        }
                    }
                }
            }
        }.getOrElse {
            MatrixCursor(projection ?: emptyArray())
        }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?,
    ): ParcelFileDescriptor =
        runCatching {
            val file = getFileForDocId(documentId)
            val accessMode = ParcelFileDescriptor.parseMode(mode)
            ParcelFileDescriptor.open(file, accessMode)
        }.getOrElse { e ->
            errorLog(LOG_TAG, e.localizedMessage ?: "Unable to open document: $documentId", e)
            CoroutineScope(Main).launch {
                context?.let { ToastUtil.showMessage(it, R.string.container_load_error) }
            }
            val (readPipe, writePipe) = ParcelFileDescriptor.createPipe()
            writePipe.close()
            return readPipe
        }

    @Throws(FileNotFoundException::class)
    private fun getFileForDocId(documentId: String?): File =
        when (documentId) {
            null -> throw FileNotFoundException("documentId cannot be null")
            ROOT_ID, DIR_SIGNATURE_CONTAINERS -> signedDocumentsFolder
            else ->
                File(signedDocumentsFolder, documentId).apply {
                    if (!exists()) {
                        throw FileNotFoundException("File not found for documentId: $documentId")
                    }
                }
        }

    private fun getDocumentId(file: File): String = if (file == signedDocumentsFolder) ROOT_ID else file.name

    private fun includeFile(
        result: MatrixCursor,
        file: File,
    ) {
        result.newRow().apply {
            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, getDocumentId(file))
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.name)
            add(DocumentsContract.Document.COLUMN_SIZE, file.length())
            add(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                if (file.isDirectory) {
                    DocumentsContract.Document.MIME_TYPE_DIR
                } else {
                    context?.let { file.mimeType(it) } ?: DEFAULT_MIME_TYPE
                },
            )
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified())
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)
        }
    }

    private fun resolveRootProjection(projection: Array<out String>?): Array<out String> =
        projection ?: arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
        )

    private fun resolveDocumentProjection(projection: Array<out String>?): Array<out String> =
        projection ?: arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
        )
}
