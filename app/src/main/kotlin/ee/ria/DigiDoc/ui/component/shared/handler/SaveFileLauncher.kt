// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.shared.handler

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utils.snackbar.SnackbarType
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

private const val LOG_TAG = "SaveFileLauncher"

@Composable
fun rememberSaveFileLauncher(onSaved: (File) -> Unit): (File, String?) -> Unit {
    val context = LocalContext.current
    val fileToSave = rememberSaveable { mutableStateOf<File?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val file = fileToSave.value
            fileToSave.value = null
            debugLog(LOG_TAG, "Save file picker closed. Result code: ${result.resultCode}")
            if (result.resultCode != Activity.RESULT_OK) {
                debugLog(LOG_TAG, "Saving file cancelled")
                return@rememberLauncherForActivityResult
            }
            val destination = result.data?.data
            if (file == null || destination == null) {
                errorLog(
                    LOG_TAG,
                    "Unable to save file. Has file: ${file != null}, has destination: ${destination != null}",
                )
                destination?.let { deleteIfEmpty(context.contentResolver, it) }
                showMessage(context, R.string.file_saved_error)
                return@rememberLauncherForActivityResult
            }
            if (saveToDocument(context.contentResolver, file, destination)) {
                debugLog(LOG_TAG, "File saved, running follow-up action")
                showMessage(context, R.string.file_saved, SnackbarType.SUCCESS)
                onSaved(file)
            } else {
                showMessage(context, R.string.file_saved_error)
            }
        }

    return remember(launcher) {
        launch@{ file, mimetype ->
            if (fileToSave.value != null) {
                debugLog(LOG_TAG, "Save file picker already open, ignoring save request")
                return@launch
            }
            fileToSave.value = file
            debugLog(LOG_TAG, "Opening save file picker. File size: ${file.length()} bytes")
            try {
                launcher.launch(
                    Intent.createChooser(
                        Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .putExtra(Intent.EXTRA_TITLE, sanitizeString(file.name, ""))
                            .setType(mimetype)
                            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                        null,
                    ),
                )
            } catch (anfe: ActivityNotFoundException) {
                fileToSave.value = null
                errorLog(LOG_TAG, "No activity to save files: ${anfe.javaClass.simpleName}")
            } catch (e: Exception) {
                fileToSave.value = null
                errorLog(LOG_TAG, "Unable to open save file picker: ${e.javaClass.simpleName}")
                throw e
            }
        }
    }
}

internal fun saveToDocument(
    contentResolver: ContentResolver,
    file: File,
    document: Uri,
): Boolean {
    val documentSize = documentSize(contentResolver, document)
    val isEmptyDocument = documentSize == 0L
    val mode = openMode(documentSize)
    debugLog(LOG_TAG, "Saving file. Destination size before save: ${sizeText(documentSize)}, open mode: $mode")
    return try {
        val bytesWritten =
            FileInputStream(file).use { input ->
                val output =
                    contentResolver.openOutputStream(document, mode)
                        ?: throw FileNotFoundException("Unable to open output stream")
                output.use { input.copyTo(it) }
            }
        debugLog(LOG_TAG, "File written. Bytes written: $bytesWritten")
        true
    } catch (e: Exception) {
        errorLog(LOG_TAG, "Unable to save file: ${e.javaClass.simpleName}")
        if (isEmptyDocument) {
            deleteDocument(contentResolver, document)
        } else {
            debugLog(LOG_TAG, "Keeping destination after failed save. Size before save: ${sizeText(documentSize)}")
        }
        false
    }
}

internal fun openMode(documentSize: Long): String = if (documentSize > 0L) "wt" else "w"

private fun deleteIfEmpty(
    contentResolver: ContentResolver,
    document: Uri,
) {
    val documentSize = documentSize(contentResolver, document)
    if (documentSize == 0L) {
        deleteDocument(contentResolver, document)
    } else {
        debugLog(LOG_TAG, "Keeping destination without a file to save. Destination size: ${sizeText(documentSize)}")
    }
}

private fun documentSize(
    contentResolver: ContentResolver,
    document: Uri,
): Long =
    try {
        contentResolver
            .query(document, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst() && index >= 0 && !cursor.isNull(index)) cursor.getLong(index) else -1L
            } ?: -1L
    } catch (e: Exception) {
        errorLog(LOG_TAG, "Unable to get document size: ${e.javaClass.simpleName}")
        -1L
    }

private fun sizeText(size: Long): String = if (size < 0L) "unknown" else "$size bytes"

private fun deleteDocument(
    contentResolver: ContentResolver,
    document: Uri,
) {
    try {
        val isDeleted = DocumentsContract.deleteDocument(contentResolver, document)
        debugLog(LOG_TAG, "Deleted empty unsaved document: $isDeleted")
    } catch (e: Exception) {
        errorLog(LOG_TAG, "Unable to delete unsaved document: ${e.javaClass.simpleName}")
    }
}
