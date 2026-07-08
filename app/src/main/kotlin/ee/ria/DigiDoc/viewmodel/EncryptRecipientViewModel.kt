/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.cryptolib.exception.DataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.RecipientsEmptyException
import ee.ria.DigiDoc.cryptolib.repository.RecipientRepository
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EncryptRecipientViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val mimeTypeResolver: MimeTypeResolver,
        private val recipientRepository: RecipientRepository,
        private val cdoc2Settings: CDOC2Settings,
        private val configurationRepository: ConfigurationRepository,
    ) : ViewModel() {
        private val logTag = "EncryptRecipientViewModel"

        private val _errorState = MutableLiveData<Int?>(null)
        val errorState: LiveData<Int?> = _errorState

        private val _queryText = MutableStateFlow("")
        val queryText = _queryText.asStateFlow()

        private val _searchText = MutableStateFlow("")
        val searchText = _searchText.asStateFlow()

        private val _recipientList = MutableStateFlow(getRecipientList())
        val recipientList = filterRecipients()

        private val _isRecipientAdded = MutableLiveData(false)
        val isRecipientAdded: LiveData<Boolean> = _isRecipientAdded

        private val _isContainerEncrypted = MutableLiveData(false)
        val isContainerEncrypted: LiveData<Boolean> = _isContainerEncrypted

        private val _hasSearched = MutableLiveData(false)
        val hasSearched: LiveData<Boolean> = _hasSearched

        private var encryptionJob: Job? = null
        private var encryptionGeneration = 0

        private val _isEncrypting = MutableStateFlow(false)
        val isEncrypting = _isEncrypting.asStateFlow()

        private val _encryptedContainer = MutableLiveData<CryptoContainer?>(null)
        val encryptedContainer: LiveData<CryptoContainer?> = _encryptedContainer

        fun handleIsRecipientAdded(isRecipientAdded: Boolean) {
            _isRecipientAdded.postValue(isRecipientAdded)
        }

        fun handleIsContainerEncrypted(isContainerEncrypted: Boolean) {
            _isContainerEncrypted.postValue(isContainerEncrypted)
        }

        fun encrypt(cryptoContainer: CryptoContainer?) {
            if (cryptoContainer == null) {
                errorLog(logTag, "Unable to encrypt: crypto container is 'null'")
                _errorState.postValue(R.string.crypto_encrypt_error)
                return
            }

            if (encryptionJob?.isActive == true) {
                return
            }

            // Cancelling cannot stop an encryption that is already running, so an old one can still be
            // finishing while a new one starts. Only the newest one returns the screen to normal when it ends.
            val generation = ++encryptionGeneration
            _isEncrypting.value = true
            encryptionJob =
                viewModelScope.launch {
                    try {
                        debugLog(logTag, "Encrypting crypto container")
                        val encrypted =
                            CryptoContainer.encrypt(
                                context = context,
                                file = cryptoContainer.file,
                                dataFiles = cryptoContainer.dataFiles,
                                recipients = cryptoContainer.recipients,
                                cdoc2Settings = cdoc2Settings,
                                configurationRepository = configurationRepository,
                            )
                        _encryptedContainer.postValue(encrypted)
                        handleIsContainerEncrypted(true)
                        debugLog(logTag, "Crypto container encrypted successfully")
                    } catch (ex: DataFilesEmptyException) {
                        errorLog(logTag, "Unable to encrypt: container has no data files", ex)
                        _errorState.postValue(R.string.crypto_encrypt_data_files_empty_error)
                    } catch (ex: RecipientsEmptyException) {
                        errorLog(logTag, "Unable to encrypt: container has no recipients", ex)
                        _errorState.postValue(R.string.crypto_encrypt_recipients_empty_error)
                    } catch (e: CancellationException) {
                        debugLog(logTag, "Encryption cancelled")
                        throw e
                    } catch (ex: Exception) {
                        errorLog(logTag, "Unable to encrypt crypto container", ex)
                        _errorState.postValue(R.string.crypto_encrypt_error)
                    } finally {
                        if (generation == encryptionGeneration) {
                            _isEncrypting.value = false
                        }
                    }
                }
        }

        fun cancelEncryption() {
            debugLog(logTag, "Cancelling encryption")
            encryptionJob?.cancel()
            encryptionJob = null
            _isEncrypting.value = false
        }

        fun resetEncryptedContainer() {
            _encryptedContainer.value = null
        }

        private fun filterRecipients() =
            queryText
                .combine(_recipientList) { text, _ ->
                    if (!text.isEmpty()) {
                        var allRecipients: Pair<List<Addressee>, Int> = Pair(listOf(), 0)
                        try {
                            allRecipients = recipientRepository.find(context, text)
                        } catch (nce: NoInternetConnectionException) {
                            errorLog(logTag, "Unable to get LDAP addressees. No Internet connection", nce)
                            _errorState.postValue(R.string.no_internet_connection)
                        } catch (e: Exception) {
                            errorLog(logTag, "Unable to get LDAP addressees", e)
                            _errorState.postValue(R.string.error_general_client)
                        }

                        if (allRecipients.second >= 50) {
                            debugLog(logTag, "Found ${allRecipients.second} addressees")
                            _errorState.postValue(R.string.crypto_recipients_too_many_results)
                        }

                        _hasSearched.postValue(true)

                        allRecipients.first
                    } else {
                        listOf()
                    }
                }.stateIn(
                    scope = viewModelScope,
                    // It will allow the StateFlow survive 5 seconds before it been canceled
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = _recipientList.value,
                )

        fun getRecipientList(): List<Addressee> = listOf<Addressee>()

        fun getContainerRecipientList(sharedContainerViewModel: SharedContainerViewModel): List<Addressee> {
            val cryptoContainer = sharedContainerViewModel.cryptoContainer.value
            return cryptoContainer?.getRecipients() ?: listOf()
        }

        fun addRecipientToContainer(
            recipient: Addressee,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            val cryptoContainer = sharedContainerViewModel.cryptoContainer.value

            cryptoContainer?.getRecipients()?.forEach {
                if (recipient.data.contentEquals(it.data)) {
                    _errorState.postValue(R.string.crypto_recipients_error_exists)
                    handleIsRecipientAdded(false)
                    return
                }
            }

            cryptoContainer?.addRecipients(listOf(recipient))

            sharedContainerViewModel.setCryptoContainer(cryptoContainer)
            handleIsRecipientAdded(true)
        }

        fun getMimetype(file: File): String? = mimeTypeResolver.mimeType(file)

        fun onSearchTextChange(text: String) {
            _queryText.value = ""
            _searchText.value = text
            _hasSearched.postValue(false)
        }

        fun onQueryTextChange(text: String) {
            _queryText.value = ""
            _queryText.value = text
            _hasSearched.postValue(false)
        }

        fun resetErrorState() {
            _errorState.postValue(null)
        }
    }
