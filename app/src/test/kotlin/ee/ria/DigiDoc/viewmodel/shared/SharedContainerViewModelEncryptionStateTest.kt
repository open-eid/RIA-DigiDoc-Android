@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SharedContainerViewModelEncryptionStateTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var contentResolver: ContentResolver

    private lateinit var viewModel: SharedContainerViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = SharedContainerViewModel(context, contentResolver)
    }

    @Test
    fun sharedContainerViewModel_containerEncrypted_initialValueIsNull() {
        assertNull(viewModel.containerEncrypted.value)
    }

    @Test
    fun sharedContainerViewModel_setCryptoContainer_setsContainerEncryptedTrueWhenContainerEncryptedTrue() {
        viewModel.setCryptoContainer(null, overwriteContainer = true, containerEncrypted = true)
        assertTrue(viewModel.containerEncrypted.value == true)
    }

    @Test
    fun sharedContainerViewModel_setCryptoContainer_doesNotSetContainerEncryptedWhenOverwriteTrueAndNoContainer() {
        viewModel.setCryptoContainer(null, overwriteContainer = true)
        assertNull(viewModel.containerEncrypted.value)
    }

    @Test
    fun sharedContainerViewModel_setCryptoContainer_doesNotSetContainerEncryptedWhenOverwriteFalseAndNoContainer() {
        viewModel.setCryptoContainer(null, overwriteContainer = false)
        assertNull(viewModel.containerEncrypted.value)
    }

    @Test
    fun sharedContainerViewModel_setCryptoContainer_doesNotSetContainerEncryptedWithNoContainer() {
        viewModel.setCryptoContainer(null)
        assertNull(viewModel.containerEncrypted.value)
    }

    @Test
    fun sharedContainerViewModel_setsValueToNullWhenContainerEncryptedIsReset() {
        viewModel.setCryptoContainer(null, overwriteContainer = true, containerEncrypted = true)
        viewModel.resetContainerEncrypted()
        assertNull(viewModel.containerEncrypted.value)
    }

    @Test
    fun sharedContainerViewModel_resetContainerEncrypted_successWhenUnchanged() {
        viewModel.resetContainerEncrypted()
        assertNull(viewModel.containerEncrypted.value)
    }

    @Test
    fun sharedContainerViewModel_containerEncrypted_valueIsTrueWhenResetAndSetAgain() {
        viewModel.setCryptoContainer(null, overwriteContainer = true, containerEncrypted = true)
        viewModel.resetContainerEncrypted()
        viewModel.setCryptoContainer(null, overwriteContainer = true, containerEncrypted = true)
        assertTrue(viewModel.containerEncrypted.value == true)
    }
}
