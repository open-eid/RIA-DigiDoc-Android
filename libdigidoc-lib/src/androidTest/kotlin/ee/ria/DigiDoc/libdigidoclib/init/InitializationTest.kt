@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.init

import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.libdigidoclib.exceptions.AlreadyInitializedException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.lang.reflect.Field

class InitializationTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        resetInitialization()
    }

    @Test
    fun testInit_success() {
        try {
            runTest {
                Initialization.init(context)
            }
        } catch (e: Exception) {
            fail("No exceptions should be thrown")
        }
    }

    @Test
    fun testInit_initWithNullContext_throwsIllegalArgumentException() {
        val mockContext = mock(Context::class.java)

        `when`(mockContext.resources).thenReturn(context.resources)
        `when`(mockContext.cacheDir).thenReturn(null)
        `when`(mockContext.filesDir).thenReturn(context.filesDir)

        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                Initialization.init(mockContext)
            }
        }
    }

    @Test
    fun testInit_initWithEmptySchemaDir_throwsIllegalArgumentException() {
        val cacheDir = context.cacheDir
        val mockContext = mock(Context::class.java)
        `when`(mockContext.resources).thenReturn(Resources.getSystem())
        `when`(mockContext.cacheDir).thenReturn(cacheDir)
        `when`(mockContext.filesDir).thenReturn(context.filesDir)

        assertThrows(NotFoundException::class.java) {
            runTest {
                Initialization.init(mockContext)
            }
        }
    }

    @Test
    fun testInit_initTwice_throwsAlreadyInitializedException() {
        assertThrows(AlreadyInitializedException::class.java) {
            runTest {
                Initialization.init(context)
                Initialization.init(context)
            }
        }
    }

    // Reset using reflection.
    // Libdigidocpp is initialized as singleton and tests might fail after first initialization
    @Throws(
        SecurityException::class,
        NoSuchFieldException::class,
        java.lang.IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    private fun resetInitialization() {
        val instance: Field = Initialization::class.java.getDeclaredField("isInitialized")
        instance.isAccessible = true
        instance.set(false, false)
    }
}
