@file:Suppress("PackageName")

package ee.ria.DigiDoc.root

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File

class RootCheckerTest {
    @Mock
    private lateinit var mockFile: File

    private lateinit var rootChecker: RootChecker

    private lateinit var rootCheckerWithDirs: RootChecker

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        rootChecker = RootCheckerImpl()
        rootCheckerWithDirs = RootCheckerImpl(listOf(mockFile))
    }

    @Test
    fun rootChecker_isRootRelatedDirectory_returnTrue() {
        `when`(mockFile.exists()).thenReturn(true)

        assertTrue(rootChecker.isRootRelatedDirectory(mockFile))
    }

    @Test
    fun rootChecker_isRootRelatedDirectory_returnFalse() {
        `when`(mockFile.exists()).thenReturn(false)

        assertFalse(rootChecker.isRootRelatedDirectory(mockFile))
    }

    @Test
    fun rootChecker_isRooted_returnTrueWhenRootRelatedFolderExists() {
        `when`(mockFile.path).thenReturn("/sbin")
        `when`(mockFile.exists()).thenReturn(true)

        assertTrue(rootCheckerWithDirs.isRooted())
    }

    @Test
    fun rootChecker_isRooted_returnFalseWhenRootRelatedFolderDoesNotExist() {
        `when`(mockFile.path).thenReturn("/sbin")
        `when`(mockFile.exists()).thenReturn(false)

        assertFalse(rootCheckerWithDirs.isRooted())
    }
}
