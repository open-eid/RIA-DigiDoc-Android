@file:Suppress("PackageName")

package ee.ria.DigiDoc.init

import ee.ria.DigiDoc.cryptolib.init.CryptoInitialization
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CryptoInitializationTest {
    companion object {
        private lateinit var cryptoInitialization: CryptoInitialization
    }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        cryptoInitialization = CryptoInitialization()
    }

    @Test
    fun initialization_init_success() {
        try {
            runTest {
                cryptoInitialization.init()
            }
        } catch (_: Exception) {
            fail("No exceptions should be thrown")
        }
    }
}
