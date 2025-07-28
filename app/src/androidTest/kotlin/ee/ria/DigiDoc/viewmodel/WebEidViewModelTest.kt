@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebEidViewModelTest {

    private lateinit var viewModel: WebEidViewModel

    @Before
    fun setUp() {
        viewModel = WebEidViewModel()
    }

    @Test
    fun handleAuth_validUri_setsAuthPayload() = runTest {
        val json = """
            {
              "challenge": "abc123",
              "login_uri": "https://example.com/auth/login",
              "get_signing_certificate": true
            }
        """.trimIndent()

        val encoded = java.util.Base64.getEncoder().encodeToString(json.toByteArray())
        val uri = Uri.parse("web-eid-mobile://auth#$encoded")

        viewModel.handleAuth(uri)

        val result = viewModel.authPayload.value
        assertEquals("abc123", result?.challenge)
        assertEquals("https://example.com/auth/login", result?.loginUri)
        assertEquals(true, result?.getSigningCertificate)
    }

    @Test
    fun handleAuth_missingFragment_setsNullPayload() = runTest {
        val uri = Uri.parse("web-eid-mobile://auth")

        viewModel.handleAuth(uri)

        assertNull(viewModel.authPayload.value)
    }

    @Test
    fun handleAuth_invalidBase64_setsNullPayload() = runTest {
        val uri = Uri.parse("web-eid-mobile://auth#invalid-base64!!")

        viewModel.handleAuth(uri)

        assertNull(viewModel.authPayload.value)
    }

    @Test
    fun handleAuth_missingOptionalField_defaultsToFalse() = runTest {
        val json = """
            {
              "challenge": "xyz456",
              "login_uri": "https://rp.example.com/login"
            }
        """.trimIndent()

        val encoded = java.util.Base64.getEncoder().encodeToString(json.toByteArray())
        val uri = Uri.parse("web-eid-mobile://auth#$encoded")

        viewModel.handleAuth(uri)

        val result = viewModel.authPayload.value
        assertEquals("xyz456", result?.challenge)
        assertEquals("https://rp.example.com/login", result?.loginUri)
        assertEquals(false, result?.getSigningCertificate)
    }
}
