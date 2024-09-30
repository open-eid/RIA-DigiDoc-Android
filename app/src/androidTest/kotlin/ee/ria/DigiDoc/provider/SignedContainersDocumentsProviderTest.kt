@file:Suppress("PackageName")

package ee.ria.DigiDoc.provider

import android.content.ContentProvider
import android.content.Context
import android.database.Cursor
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import androidx.test.core.app.ApplicationProvider
import ee.ria.DigiDoc.common.Constant.ASICE_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DIR_SIGNATURE_CONTAINERS
import ee.ria.DigiDoc.common.testfiles.file.TestFileUtil.Companion.createZipWithTextFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.name

@RunWith(MockitoJUnitRunner::class)
class SignedContainersDocumentsProviderTest {
    private lateinit var provider: SignedContainersDocumentsProvider
    private lateinit var mockContext: Context
    private lateinit var mockSignedDocumentsFolder: File

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)

        mockSignedDocumentsFolder =
            mock(File::class.java).apply {
                `when`(exists()).thenReturn(true)
                `when`(listFiles()).thenReturn(arrayOf())
                `when`(path).thenReturn("signed_containers")
            }

        `when`(mockContext.filesDir).thenReturn(mockSignedDocumentsFolder)

        provider =
            SignedContainersDocumentsProvider().apply {
                setTestContext(ApplicationProvider.getApplicationContext<Context>())
                onCreate()
            }
    }

    private fun SignedContainersDocumentsProvider.setTestContext(context: Context) {
        val field = ContentProvider::class.java.getDeclaredField("mContext")
        field.isAccessible = true
        field.set(this, context)
    }

    @Test
    fun testNoContext() {
        SignedContainersDocumentsProvider().apply {
            assertFalse(onCreate())
        }
    }

    @Test
    fun testQueryRoots() {
        val cursor: Cursor = provider.queryRoots(null)
        assertNotNull(cursor)

        cursor.use {
            it.moveToFirst()
            assertEquals(DocumentsContract.Root.COLUMN_ROOT_ID, it.getColumnName(0))
            assertEquals("RIA DigiDoc (debug)", it.getString(it.getColumnIndex(DocumentsContract.Root.COLUMN_TITLE)))
        }
    }

    @Test
    fun testQueryDocument_withValidId() {
        val cursor: Cursor = provider.queryDocument("root", null)
        assertNotNull(cursor)

        cursor.use {
            it.moveToFirst()
            assertEquals(
                "signed_containers",
                it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)),
            )
            assertEquals(4096L, it.getLong(it.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)))
        }
    }

    @Test
    fun testQueryDocument_withValidDocId() {
        val tempFile = File.createTempFile("test", ".txt", File("${ApplicationProvider.getApplicationContext<Context>().filesDir}/$DIR_SIGNATURE_CONTAINERS"))
        tempFile.writeText("TestText", StandardCharsets.UTF_8)

        tempFile.deleteOnExit()

        val cursor: Cursor = provider.queryDocument(tempFile.name, null)
        assertNotNull(cursor)

        cursor.use {
            it.moveToFirst()
            assertEquals(
                tempFile.name,
                it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)),
            )
            assertEquals(tempFile.length(), it.getLong(it.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)))
        }
    }

    @Test
    fun testQueryDocument_withInvalidId() {
        val cursor: Cursor = provider.queryDocument("randomDocument", null)

        assertNotNull(cursor)
        assertTrue(cursor.columnNames.isEmpty())
        assertEquals(-1, cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
    }

    @Test
    fun testQueryDocument_withNoId() {
        val mockFile = createZipWithTextFile(ASICE_MIMETYPE)

        `when`(mockSignedDocumentsFolder.listFiles()).thenReturn(arrayOf(mockFile))

        val cursor: Cursor = provider.queryDocument(null, null)

        assertNotNull(cursor)
        assertTrue(cursor.columnNames.isEmpty())
        assertEquals(-1, cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
    }

    @Test
    fun testQueryChildDocuments() {
        val tempFile = createZipWithTextFile(ASICE_MIMETYPE, "mimetype")
        tempFile.deleteOnExit()

        val signatureContainersDirectory = File(ApplicationProvider.getApplicationContext<Context>().filesDir, DIR_SIGNATURE_CONTAINERS)
        val sourcePath = Paths.get(tempFile.path)
        val targetPath = Paths.get("${signatureContainersDirectory.path}/${sourcePath.fileName.name}")
        val movedFile = Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
        sourcePath.deleteIfExists()

        val cursor: Cursor = provider.queryChildDocuments("root", null, sortOrder = "_display_name ASC")
        assertNotNull(cursor)

        val fileNamesInDirectory = signatureContainersDirectory.listFiles()?.map { it.name } ?: emptyList()
        assertTrue(fileNamesInDirectory.contains(movedFile.fileName.name))

        var fileNameExistsInCursor = false
        var fileSizeMatches = false

        cursor.use {
            while (it.moveToNext()) {
                val displayName = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                val fileSize = it.getLong(it.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE))

                if (displayName == movedFile.fileName.name) {
                    fileNameExistsInCursor = true
                    fileSizeMatches = (fileSize == movedFile.fileSize())
                    break
                }
            }
        }

        assertTrue(fileNameExistsInCursor)
        assertTrue(fileSizeMatches)

        targetPath.deleteIfExists()
        movedFile.deleteIfExists()
    }

    @Test
    fun testQueryChildDocumentsNot() {
        val cursor: Cursor = provider.queryChildDocuments(null, null, sortOrder = null)
        assertNotNull(cursor)

        cursor.use {
            it.moveToFirst()
            assertTrue(cursor.columnNames.isEmpty())
            assertEquals(-1, it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
            assertEquals(-1, it.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE))
        }
    }

    @Test
    fun testOpenDocument_withValidId() {
        val tempFile = File.createTempFile("test", ".txt",
            File("${ApplicationProvider.getApplicationContext<Context>().filesDir}/$DIR_SIGNATURE_CONTAINERS"))
        tempFile.writeText("TestText", StandardCharsets.UTF_8)

        tempFile.deleteOnExit()

        val fileDescriptor: ParcelFileDescriptor = provider.openDocument(tempFile.name, "r", null)

        assertNotNull(fileDescriptor)
        assertTrue(fileDescriptor.fileDescriptor.valid())
        assertEquals(8, fileDescriptor.statSize)

        fileDescriptor.close()
    }

    @Test
    fun testOpenDocument_withInvalidId() {
        val parcelFileDescriptor = provider.openDocument("invalidId", "r", null)

        assertNotNull(parcelFileDescriptor)
        assertTrue(parcelFileDescriptor.fileDescriptor.valid())
        assertEquals(-1, parcelFileDescriptor.statSize)
    }
}
