// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.shared.handler

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class SaveFileLauncherTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var contentResolver: ContentResolver
    private lateinit var directory: File

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = context.contentResolver
        directory = File(context.cacheDir, "saveFileLauncherTest").apply { mkdirs() }
    }

    @After
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun saveToDocument_copiesFileContentToDocument() {
        val source = File(directory, "source.asice").apply { writeText("container content") }
        val destination = File(directory, "destination.asice").apply { createNewFile() }

        val isSaved = saveToDocument(contentResolver, source, Uri.fromFile(destination))

        assertTrue(isSaved)
        assertEquals("container content", destination.readText())
    }

    @Test
    fun saveToDocument_leavesDestinationUntouchedWhenSourceIsMissing() {
        val source = File(directory, "missing.asice")
        val destination = File(directory, "destination.asice").apply { writeText("user content") }

        val isSaved = saveToDocument(contentResolver, source, Uri.fromFile(destination))

        assertFalse(isSaved)
        assertEquals("user content", destination.readText())
    }

    @Test
    fun saveToDocument_overwritesLongerExistingDocumentWithExactContent() {
        val source = File(directory, "source.asice").apply { writeText("short") }
        val destination = File(directory, "destination.asice").apply { writeText("a much longer existing content") }

        val isSaved = saveToDocument(contentResolver, source, contentUri(destination))

        assertTrue(isSaved)
        assertEquals("short", destination.readText())
    }

    @Test
    fun saveToDocument_writesFullContentToNewEmptyDocument() {
        val source = File(directory, "source.asice").apply { writeText("container content") }
        val destination = File(directory, "destination.asice").apply { createNewFile() }

        val isSaved = saveToDocument(contentResolver, source, contentUri(destination))

        assertTrue(isSaved)
        assertEquals("container content", destination.readText())
    }

    @Test
    fun saveFile_ignoresSecondSaveWhilePickerIsOpen() {
        val registry = RecordingActivityResultRegistry()
        val saveFile = setSaveFileLauncher(registry)

        composeTestRule.runOnIdle {
            saveFile(File(directory, "container.asice"), "application/vnd.etsi.asic-e+zip")
            saveFile(File(directory, "container.asice"), "application/vnd.etsi.asic-e+zip")
        }

        assertEquals(1, registry.launchedRequestCodes.size)
    }

    @Test
    fun saveFile_allowsNextSaveAfterPickerReturns() {
        val registry = RecordingActivityResultRegistry()
        val saveFile = setSaveFileLauncher(registry)

        composeTestRule.runOnIdle {
            saveFile(File(directory, "container.asice"), "application/vnd.etsi.asic-e+zip")
            registry.dispatchResult(registry.launchedRequestCodes.first(), Activity.RESULT_CANCELED, null)
            saveFile(File(directory, "container.asice"), "application/vnd.etsi.asic-e+zip")
        }

        assertEquals(2, registry.launchedRequestCodes.size)
    }

    @Test
    fun saveFile_allowsNextSaveAfterPickerFailsToLaunch() {
        val registry = RecordingActivityResultRegistry(launchError = ActivityNotFoundException())
        val saveFile = setSaveFileLauncher(registry)

        composeTestRule.runOnIdle {
            saveFile(File(directory, "container.asice"), "application/vnd.etsi.asic-e+zip")
            registry.launchError = null
            saveFile(File(directory, "container.asice"), "application/vnd.etsi.asic-e+zip")
        }

        assertEquals(1, registry.launchedRequestCodes.size)
    }

    @Test
    fun saveFile_reportsSavedFileAfterSuccessfulSave() {
        val registry = RecordingActivityResultRegistry()
        var savedFile: File? = null
        val saveFile = setSaveFileLauncher(registry) { savedFile = it }
        val source = File(directory, "source.asice").apply { writeText("container content") }
        val destination = File(directory, "destination.asice").apply { createNewFile() }

        composeTestRule.runOnIdle {
            saveFile(source, "application/vnd.etsi.asic-e+zip")
            registry.dispatchResult(
                registry.launchedRequestCodes.first(),
                Activity.RESULT_OK,
                Intent().setData(contentUri(destination)),
            )
        }

        assertEquals(source, savedFile)
        assertEquals("container content", destination.readText())
    }

    private fun contentUri(file: File): Uri {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), file)
    }

    private fun setSaveFileLauncher(
        registry: ActivityResultRegistry,
        onSaved: (File) -> Unit = {},
    ): (File, String?) -> Unit {
        lateinit var saveFile: (File, String?) -> Unit
        val owner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                saveFile = rememberSaveFileLauncher(onSaved = onSaved)
            }
        }
        return saveFile
    }

    private class RecordingActivityResultRegistry(
        var launchError: RuntimeException? = null,
    ) : ActivityResultRegistry() {
        val launchedRequestCodes = mutableListOf<Int>()

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            launchError?.let { throw it }
            launchedRequestCodes.add(requestCode)
        }
    }
}
