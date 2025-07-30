@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class WebEidAuthServiceTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var parser: WebEidAuthParser
    private lateinit var service: WebEidAuthService

    @Before
    fun setup() {
        parser = WebEidAuthParser()
        service = WebEidAuthService(parser)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseAuthUri_validUri_updatesAuthRequest() = runTest {
        val uri = createAuthUri(
            challenge = "abc123",
            loginUri = "https://rp.example.com/auth/eid/login",
            getCert = true
        )

        service.parseAuthUri(uri)

        val auth = service.authRequest.value
        assertEquals("abc123", auth?.challenge)
        assertEquals("https://rp.example.com/auth/eid/login", auth?.loginUri)
        assertEquals(true, auth?.getSigningCertificate)
        assertEquals("https://rp.example.com", auth?.origin)
        assertNull(service.errorState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseSignUri_validUri_updatesSignRequest() = runTest {
        val uri = createSignUri(
            responseUri = "https://rp.example.com/sign/ok",
            signCert = "CERTDATA",
            hash = "abcd1234",
            hashFunc = "SHA-256"
        )

        service.parseSignUri(uri)

        val sign = service.signRequest.value
        assertEquals("https://rp.example.com/sign/ok", sign?.responseUri)
        assertEquals("CERTDATA", sign?.signCertificate)
        assertEquals("abcd1234", sign?.hash)
        assertEquals("SHA-256", sign?.hashFunction)
        assertNull(service.errorState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resetValues_clearsAllState() = runTest {
        val uri = createAuthUri("abc123", "https://rp.example.com", false)
        service.parseAuthUri(uri)

        service.resetValues()

        assertNull(service.authRequest.value)
        assertNull(service.signRequest.value)
        assertNull(service.errorState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseAuthUri_invalidUri_setsErrorState() = runTest {
        val badUri = Uri.parse("web-eid://auth#not-base64!!!")

        service.parseAuthUri(badUri)

        assertNull(service.authRequest.value)
        assertNull(service.signRequest.value)
        assert(service.errorState.value != null)
    }

    @Suppress("SameParameterValue")
    private fun createAuthUri(challenge: String, loginUri: String, getCert: Boolean): Uri {
        val json = """
            {
              "challenge": "$challenge",
              "login_uri": "$loginUri",
              "get_signing_certificate": $getCert
            }
        """.trimIndent()
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        return Uri.parse("web-eid://auth#$encoded")
    }

    @Suppress("SameParameterValue")
    private fun createSignUri(responseUri: String, signCert: String, hash: String, hashFunc: String): Uri {
        val json = """
            {
              "response_uri": "$responseUri",
              "sign_certificate": "$signCert",
              "hash": "$hash",
              "hash_function": "$hashFunc"
            }
        """.trimIndent()
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        return Uri.parse("web-eid://sign#$encoded")
    }
}
