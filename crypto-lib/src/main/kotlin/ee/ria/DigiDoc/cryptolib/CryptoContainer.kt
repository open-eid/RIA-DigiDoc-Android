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

package ee.ria.DigiDoc.cryptolib

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.common.Constant.CDOC1_EXTENSION
import ee.ria.DigiDoc.common.Constant.CDOC2_EXTENSION
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.container.Container
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.exception.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.CryptoException
import ee.ria.DigiDoc.cryptolib.exception.DataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.RecipientsEmptyException
import ee.ria.DigiDoc.cryptolib.exception.WrongPasswordException
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.network.utils.ProxyUtil
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.extensions.isCryptoContainer
import ee.ria.DigiDoc.utilsLib.extensions.saveAs
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.file.FileUtil.uniqueFileName
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.cdoc.CDoc
import ee.ria.cdoc.CDocException
import ee.ria.cdoc.CDocReader
import ee.ria.cdoc.CDocWriter
import ee.ria.cdoc.CertificateList
import ee.ria.cdoc.Configuration
import ee.ria.cdoc.CryptoBackend
import ee.ria.cdoc.DataBuffer
import ee.ria.cdoc.FileInfo
import ee.ria.cdoc.Lock
import ee.ria.cdoc.Lock.parseLabel
import ee.ria.cdoc.LogLevel
import ee.ria.cdoc.Logger
import ee.ria.cdoc.NetworkBackend
import ee.ria.cdoc.Recipient
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

private const val LOG_TAG = "CryptoContainer"
private const val PASSWORD_KDF_ITER = 600000 // OWASP recommended minimum for PBKDF2-HMAC-SHA256

@Singleton
class CryptoContainer
    @Inject
    constructor(
        val context: Context,
        var file: File,
        val dataFiles: ArrayList<File>,
        val recipients: ArrayList<Addressee>,
        val decrypted: Boolean,
        val encrypted: Boolean,
        val isExistingContainer: Boolean = false,
    ) : Container {
        fun containerMimetype(): String = CONTAINER_MIME_TYPE

        fun getName(): String = file.name

        suspend fun setName(filename: String) {
            val name = sanitizeString(filename, "")
            val containerName = ContainerUtil.addExtensionToContainerFilename(name)
            val newFile = File(file.parent, containerName)

            withContext(IO) {
                file.renameTo(newFile)
                file = newFile
            }
        }

        fun addDataFiles(filesToAdd: List<File>) {
            dataFiles.addAll(filesToAdd)
        }

        fun addRecipients(recipientsToAdd: List<Addressee>) {
            recipients.addAll(recipientsToAdd)
        }

        fun getDataFiles(): List<File> =
            dataFiles
                .toList()

        fun getRecipients(): List<Addressee> =
            recipients
                .toList()

        fun hasRecipients(): Boolean = recipients.isNotEmpty()

        @Throws(Exception::class)
        fun getDataFile(
            dataFile: File,
            directory: File,
        ): File {
            val tmp = sanitizeString(dataFile.name, "")
            val file = File(directory, tmp)

            for (containerDataFile in dataFiles) {
                if (dataFile.name == containerDataFile.name) {
                    if (!file.exists()) {
                        containerDataFile.saveAs(file.absolutePath)
                    }
                    return file
                }
            }
            throw IllegalArgumentException("Could not find file ${dataFile.name} in container ${file.name}")
        }

        @Throws(Exception::class)
        suspend fun removeDataFile(dataFile: File) {
            if (dataFiles.size == 1) {
                throw ContainerDataFilesEmptyException()
            }

            withContext(IO) {
                val index = dataFiles.indexOfFirst { it.name == dataFile.name }
                if (index != -1) {
                    dataFiles.removeAt(index)
                }
            }
        }

        @Throws(Exception::class)
        suspend fun removeRecipient(recipient: Addressee) {
            if (recipients.isNotEmpty()) {
                withContext(IO) {
                    val index = recipients.indexOfFirst { it.identifier == recipient.identifier }
                    if (index != -1) {
                        recipients.removeAt(index)
                    }
                }
            }
        }

        companion object {
            val logger = JavaLogger()

            private val libcdocLogLevel = LogLevel.LEVEL_TRACE

            // Encryption can pause and resume midway, so it needs the coroutine-friendly Mutex.
            // Decryption runs start-to-finish in one go, so a plain lock (ReentrantLock) is enough.
            private val encryptOperation = Mutex()
            private val decryptOperation = ReentrantLock()

            private fun parsedLockLabel(label: String?): String =
                try {
                    parseLabel(label.orEmpty())["label"].orEmpty()
                } catch (_: Exception) {
                    label.orEmpty()
                }

            @Throws(CryptoException::class)
            private suspend fun open(
                context: Context,
                file: File,
            ): CryptoContainer {
                debugLog(LOG_TAG, "Opening crypto container: ${file.name} (extension ${file.extension})")
                val cdoc1 = if (file.extension == CDOC1_EXTENSION) parseCdoc1(file) else null
                val dataFiles = cdoc1?.dataFileNames?.map { File(it) }.orEmpty()
                debugLog(LOG_TAG, "Parsed CDOC1 content: ${cdoc1 != null}, data file count: ${dataFiles.size}")

                val lockAddressees =
                    withContext(IO) {
                        val cdocReader = CDocReader.createReader(file.path, null, null, null)
                        debugLog(LOG_TAG, "Reader created: (version ${cdocReader.version})")
                        try {
                            cdocReader.locks.mapIndexed { index, lock -> addresseeOf(lock, index) }
                        } finally {
                            cdocReader.delete()
                        }
                    }

                val cdoc1Recipients = cdoc1?.recipientCertificates?.map { Addressee.fromCert(it) }.orEmpty()
                val recipients =
                    if (cdoc1Recipients.isNotEmpty()) {
                        cdoc1Recipients.map { recipient ->
                            lockAddressees
                                .firstOrNull { it.data.contentEquals(recipient.data) }
                                ?.let { recipient.copy(concatKDFAlgorithmURI = it.concatKDFAlgorithmURI) }
                                ?: recipient
                        }
                    } else {
                        lockAddressees
                    }
                debugLog(LOG_TAG, "Resolved ${recipients.size} recipient(s) for container ${file.name}")

                return create(
                    context,
                    file,
                    dataFiles,
                    recipients,
                    decrypted = false,
                    encrypted = true,
                    isExistingContainer = true,
                )
            }

            private fun addresseeOf(
                lock: Lock,
                lockIndex: Int,
            ): Addressee {
                debugLog(
                    LOG_TAG,
                    "Mapping lock to addressee with label ${lock.label}. " +
                        "Is CDOC1: ${lock.isCDoc1}, is PKI: ${lock.isPKI}, is symmetric: ${lock.isSymmetric}",
                )
                val addressee =
                    when {
                        lock.isCDoc1 -> {
                            val certAddressee = Addressee.fromCert(lock.getBytes(Lock.Params.CERT) ?: ByteArray(0))
                            if (lock.isRSA) {
                                certAddressee
                            } else {
                                certAddressee.copy(concatKDFAlgorithmURI = lock.getString(Lock.Params.CONCAT_DIGEST))
                            }
                        }
                        lock.isPKI ->
                            Addressee.fromLabel(
                                lock.label,
                                if (lock.type == Lock.Type.PUBLIC_KEY) {
                                    lock.getBytes(Lock.Params.RCPT_KEY) ?: ByteArray(0)
                                } else {
                                    ByteArray(0)
                                },
                                "",
                            )
                        lock.type == Lock.Type.PASSWORD ->
                            Addressee(
                                data = ByteArray(0),
                                identifier = parsedLockLabel(lock.label),
                                serialNumber = null,
                                givenName = null,
                                surname = null,
                                certType = CertType.PasswordType,
                                validTo = null,
                                concatKDFAlgorithmURI = null,
                                lockLabel = lock.label,
                                lockType = lock.type.name,
                            )
                        lock.isSymmetric ->
                            Addressee.fromCN(lock.label, "", CertType.UnknownType, null, ByteArray(0))
                        else -> {
                            debugLog(LOG_TAG, "Unknown lock type for label ${lock.label}, mapping to 'Unknown capsule'")
                            Addressee.fromLabel("Unknown capsule", ByteArray(0), "")
                        }
                    }.copy(keyLabel = lock.label.takeIf { it.isNotBlank() }, lockIndex = lockIndex)

                if (lock.type != Lock.Type.SERVER) {
                    return addressee
                }
                return addressee.copy(
                    serverId = lock.getString(Lock.Params.KEYSERVER_ID).takeIf { it.isNotBlank() },
                    transactionId = lock.getString(Lock.Params.TRANSACTION_ID).takeIf { it.isNotBlank() },
                )
            }

            @Throws(CryptoException::class)
            private suspend fun parseCdoc1(file: File): Cdoc1Content =
                withContext(IO) {
                    debugLog(LOG_TAG, "Parsing CDOC1 container: ${file.name}")
                    try {
                        FileInputStream(file).use { Cdoc1Parser.parse(it) }
                    } catch (e: Exception) {
                        errorLog(LOG_TAG, "Can't open crypto container: ${e.message}", e)
                        throw CryptoException("Can't open crypto container", e)
                    }
                }

            @Throws(CryptoException::class, SmartCardReaderException::class)
            fun decrypt(
                context: Context,
                file: File,
                recipients: List<Addressee>,
                authCert: ByteArray?,
                pin: ByteArray,
                smartToken: Token,
                cdoc2Settings: CDOC2Settings,
                configurationRepository: ConfigurationRepository,
            ): CryptoContainer =
                decryptOperation.withLock {
                    val token = SmartCardTokenWrapper(pin, smartToken)
                    val conf = CryptoLibConf(cdoc2Settings)
                    val configurationProvider = configurationRepository.getConfiguration()

                    if (authCert == null || authCert.isEmpty()) {
                        throw CryptoException("Failed to get auth certificate")
                    }
                    val network =
                        CryptoLibNetworkBackend(cdoc2Settings, configurationProvider, context, authCert, token)
                    val dataFiles = ArrayList<File>()

                    val cdocReader = CDocReader.createReader(file.path, conf, token, network)
                    debugLog(LOG_TAG, "Reader created: (version ${cdocReader.version})")
                    try {
                        val idx = cdocReader.getLockForCert(authCert)

                        if (idx < 0) {
                            throw CryptoException("Failed to get lock for certificate")
                        }

                        val fmk = cdocReader.getFMK(idx.toInt())

                        if (token.lastError != null) {
                            throw token.lastError as Throwable
                        }

                        if (fmk.isEmpty()) {
                            throw CryptoException("Failed to get FMK")
                        }

                        if (cdocReader.beginDecryption(fmk) != 0L) {
                            throw CryptoException("Failed to begin decryption")
                        }
                        debugLog(LOG_TAG, "Decryption started for container ${file.name}")

                        val fileInfo = FileInfo()
                        val savedNames = mutableSetOf<String>()
                        val dir = ContainerUtil.getContainerDataFilesDir(context, file)

                        // Some file names crash the app if libcdoc logs them while reading
                        logger.setMinLogLevel(LogLevel.LEVEL_INFO)

                        var result: Long = cdocReader.nextFile(fileInfo)
                        while (result == CDoc.OK.toLong()) {
                            val ofile = File(fileInfo.name)
                            val tmp = uniqueFileName(sanitizeString(ofile.name, ""), savedNames)
                            savedNames.add(tmp)
                            val fileToSave = File(dir, tmp)
                            FileOutputStream(fileToSave).use { ofs ->
                                cdocReader.readFile(ofs)
                            }
                            dataFiles.add(fileToSave)
                            debugLog(LOG_TAG, "Decrypted data file: $tmp")
                            result = cdocReader.nextFile(fileInfo)
                        }

                        if (cdocReader.finishDecryption() != 0L) {
                            throw CryptoException("Failed to finish decryption")
                        }
                        debugLog(LOG_TAG, "Decryption finished, ${dataFiles.size} data file(s) extracted")

                        create(
                            context,
                            file,
                            dataFiles,
                            recipients,
                            decrypted = true,
                            encrypted = false,
                        )
                    } catch (exc: IOException) {
                        errorLog(LOG_TAG, "IO Exception while decrypting container: ${exc.message}", exc)
                        throw CryptoException("IO Exception: ${exc.message}", exc)
                    } finally {
                        logger.setMinLogLevel(libcdocLogLevel)
                        cdocReader.delete()
                    }
                }

            @Throws(CryptoException::class)
            suspend fun decryptWithPassword(
                context: Context,
                file: File,
                recipients: List<Addressee>,
                password: ByteArray,
                cdoc2Settings: CDOC2Settings,
                configurationRepository: ConfigurationRepository,
                lockIndex: Int? = null,
            ): CryptoContainer {
                val backend = PasswordCryptoBackend(password)
                val conf = CryptoLibConf(cdoc2Settings)
                val configurationProvider = configurationRepository.getConfiguration()
                val network = Network(cdoc2Settings, configurationProvider, context)

                debugLog(LOG_TAG, "Decrypting '${file.name}' with password")

                return withContext(IO) {
                    decryptOperation.withLock {
                        val cdocReader =
                            CDocReader.createReader(file.path, conf, backend, network)
                        if (cdocReader == null) throw WrongPasswordException()
                        try {
                            val locks = cdocReader.locks.toList()
                            val selectedLock = lockIndex?.takeIf { locks.getOrNull(it)?.type == Lock.Type.PASSWORD }
                            if (lockIndex != null && selectedLock == null) {
                                errorLog(
                                    LOG_TAG,
                                    "Password lock index $lockIndex is not a password lock in '${file.name}' " +
                                        "(${locks.size} lock(s)) — falling back to the first password lock",
                                )
                            }
                            val lockIdx = selectedLock ?: locks.indexOfFirst { it.type == Lock.Type.PASSWORD }
                            if (lockIdx < 0) throw CryptoException("No password lock found in container")
                            debugLog(LOG_TAG, "Using password lock at index $lockIdx")

                            val fmk = cdocReader.getFMK(lockIdx)
                            if (fmk == null || fmk.isEmpty()) throw WrongPasswordException()

                            if (cdocReader.beginDecryption(fmk) !=
                                0L
                            ) {
                                throw CryptoException("Failed to begin decryption")
                            }
                            val dataFiles =
                                try {
                                    decryptFiles(context, file, cdocReader)
                                } catch (exc: CDocException) {
                                    errorLog(LOG_TAG, "Wrong password entered for '${file.name}'")
                                    throw WrongPasswordException(exc)
                                }
                            if (cdocReader.finishDecryption() !=
                                0L
                            ) {
                                throw CryptoException("Failed to finish decryption")
                            }

                            debugLog(
                                LOG_TAG,
                                "Successfully decrypted '${file.name}' — ${dataFiles.size} file(s) extracted",
                            )
                            create(context, file, dataFiles, recipients, decrypted = true, encrypted = false)
                        } finally {
                            cdocReader.delete()
                        }
                    }
                }
            }

            @Throws(CryptoException::class)
            private fun decryptFiles(
                context: Context,
                containerFile: File,
                cdocReader: CDocReader,
            ): ArrayList<File> {
                val dataFiles = ArrayList<File>()
                val fi = FileInfo()
                val savedNames = mutableSetOf<String>()
                val dir = ContainerUtil.getContainerDataFilesDir(context, containerFile)

                // Some file names crash the app if libcdoc logs them while reading
                logger.setMinLogLevel(LogLevel.LEVEL_INFO)
                try {
                    var result: Long = cdocReader.nextFile(fi)
                    while (result == CDoc.OK.toLong()) {
                        val tmp = uniqueFileName(sanitizeString(File(fi.name).name, ""), savedNames)
                        savedNames.add(tmp)
                        val fileToSave = File(dir, tmp)
                        FileOutputStream(fileToSave).use { ofs -> cdocReader.readFile(ofs) }
                        dataFiles.add(fileToSave)
                        debugLog(LOG_TAG, "Decrypted data file: $tmp")
                        result = cdocReader.nextFile(fi)
                    }
                } catch (exc: IOException) {
                    throw CryptoException("IO Exception: ${exc.message}", exc)
                } finally {
                    logger.setMinLogLevel(libcdocLogLevel)
                }
                return dataFiles
            }

            @Throws(CryptoException::class)
            private suspend fun writeEncryptedContainer(
                context: Context,
                file: File,
                dataFiles: List<File>,
                createWriter: () -> CDocWriter,
                addRecipients: suspend (CDocWriter) -> Unit,
            ): CryptoContainer =
                withContext(IO) {
                    encryptOperation.withLock {
                        val cdocWriter = createWriter()
                        var encryptionFinished = false
                        try {
                            addRecipients(cdocWriter)

                            currentCoroutineContext().ensureActive()

                            if (cdocWriter.beginEncryption() != 0L) {
                                throw CryptoException("Failed to begin encryption")
                            }

                            dataFiles.forEach { dataFile ->
                                currentCoroutineContext().ensureActive()
                                val bytes = FileInputStream(dataFile).use { it.readBytes() }
                                if (cdocWriter.addFile(dataFile.name, bytes.size.toLong()) != 0L) {
                                    throw CryptoException("Failed to add file")
                                }

                                if (cdocWriter.writeData(bytes) != 0L) {
                                    throw CryptoException("Failed to write data")
                                }
                            }

                            currentCoroutineContext().ensureActive()

                            if (cdocWriter.finishEncryption() != 0L) {
                                throw CryptoException("Failed to finish encryption")
                            }
                            encryptionFinished = true
                            debugLog(LOG_TAG, "Successfully encrypted '${file.name}'")
                        } catch (exc: IOException) {
                            errorLog(LOG_TAG, "IO Exception: ${exc.message}", exc)
                            throw CryptoException("IO Exception: ${exc.message}", exc)
                        } catch (exc: CDocException) {
                            errorLog(LOG_TAG, "CDoc Exception ${exc.code}: ${exc.message}", exc)
                            throw CryptoException("CDoc Exception ${exc.code}: ${exc.message}", exc)
                        } finally {
                            cdocWriter.delete()
                            if (!encryptionFinished) {
                                // A cancelled or failed run leaves a half-written container behind.
                                // Emptying it restores the placeholder the container started as, so it
                                // cannot show up as a broken entry in recent documents.
                                try {
                                    FileOutputStream(file).use { }
                                } catch (exc: IOException) {
                                    errorLog(LOG_TAG, "Unable to empty unfinished container file", exc)
                                }
                            }
                        }

                        open(context, file)
                    }
                }

            @Throws(CryptoException::class)
            suspend fun encryptWithPassword(
                context: Context,
                file: File,
                dataFiles: List<File>,
                keyLabel: String,
                password: ByteArray,
                cdoc2Settings: CDOC2Settings,
                configurationRepository: ConfigurationRepository,
            ): CryptoContainer {
                if (dataFiles.isEmpty()) {
                    throw DataFilesEmptyException("Cannot create an empty crypto container")
                }
                val backend = PasswordCryptoBackend(password)
                val conf = CryptoLibConf(cdoc2Settings)
                val configurationProvider = configurationRepository.getConfiguration()
                val network = Network(cdoc2Settings, configurationProvider, context)

                debugLog(LOG_TAG, "Encrypting '${file.name}' with password, key label: '$keyLabel'")

                return writeEncryptedContainer(
                    context = context,
                    file = file,
                    dataFiles = dataFiles,
                    createWriter = { CDocWriter.createWriter(2, file.path, conf, backend, network) },
                    addRecipients = { cdocWriter ->
                        val recipient = Recipient.makeSymmetric("", PASSWORD_KDF_ITER)
                        recipient.setLabelValue("label", keyLabel)
                        if (cdocWriter.addRecipient(recipient) != 0L) {
                            throw CryptoException("Failed to add password recipient")
                        }
                    },
                )
            }

            suspend fun encrypt(
                context: Context,
                file: File,
                dataFiles: List<File>,
                recipients: List<Addressee>,
                cdoc2Settings: CDOC2Settings,
                configurationRepository: ConfigurationRepository,
            ): CryptoContainer {
                if (dataFiles.isEmpty()) {
                    throw DataFilesEmptyException("Cannot create an empty crypto container")
                }

                if (recipients.isEmpty()) {
                    throw RecipientsEmptyException("Cannot create crypto container without recipients")
                }

                val configurationProvider = configurationRepository.getConfiguration()
                val conf = CryptoLibConf(cdoc2Settings)
                val network = Network(cdoc2Settings, configurationProvider, context)

                val version =
                    if (file.extension == CDOC2_EXTENSION) {
                        2
                    } else {
                        1
                    }
                val useOnlineEncryption = version == 2 && cdoc2Settings.getUseOnlineEncryption()

                debugLog(
                    LOG_TAG,
                    "Encrypting container (CDOC version $version, " +
                        "online key transfer: $useOnlineEncryption)",
                )

                return writeEncryptedContainer(
                    context = context,
                    file = file,
                    dataFiles = dataFiles,
                    createWriter = { CDocWriter.createWriter(version, file.path, conf, null, network) },
                    addRecipients = { cdocWriter ->
                        val serverId = if (useOnlineEncryption) cdoc2Settings.getCDOC2UUID() else null
                        recipients.forEach { addressee ->
                            currentCoroutineContext().ensureActive()
                            val recipient =
                                if (serverId != null) {
                                    Recipient.makeCertificate("", addressee.data, serverId)
                                } else {
                                    Recipient.makeCertificate("", addressee.data)
                                }
                            if (cdocWriter.addRecipient(recipient) != 0L) {
                                throw CryptoException("Failed to add recipient")
                            }
                        }
                    },
                )
            }

            @Throws(CryptoException::class)
            private fun create(
                context: Context,
                file: File,
                dataFiles: List<File>,
                recipients: List<Addressee>,
                decrypted: Boolean,
                encrypted: Boolean,
                isExistingContainer: Boolean = false,
            ): CryptoContainer =
                CryptoContainer(
                    context,
                    file,
                    ArrayList(dataFiles),
                    ArrayList(recipients),
                    decrypted,
                    encrypted,
                    isExistingContainer,
                )

            @Throws(Exception::class)
            suspend fun openOrCreate(
                @ApplicationContext context: Context,
                file: File,
                dataFiles: List<File>,
                cdoc2Settings: CDOC2Settings,
                forceCreate: Boolean = false,
            ): CryptoContainer {
                if (dataFiles.isEmpty()) {
                    throw DataFilesEmptyException("Cannot create an empty crypto container")
                }

                val isFirstDataFileContainer =
                    dataFiles.firstOrNull()?.run {
                        isCryptoContainer()
                    } == true

                var containerFileWithExtension = file

                if (!isFirstDataFileContainer) {
                    val defaultExtension =
                        if (cdoc2Settings.getUseEncryption()) {
                            CDOC2_EXTENSION
                        } else {
                            CDOC1_EXTENSION
                        }

                    containerFileWithExtension =
                        File(
                            FilenameUtils.removeExtension(file.path) + ".$defaultExtension",
                        )

                    containerFileWithExtension.parentFile?.mkdirs()

                    if (!containerFileWithExtension.exists()) {
                        containerFileWithExtension.createNewFile()
                    }
                }

                return if (!forceCreate && dataFiles.size == 1 && isFirstDataFileContainer) {
                    open(context, containerFileWithExtension)
                } else {
                    create(
                        context,
                        containerFileWithExtension,
                        dataFiles,
                        listOf(),
                        decrypted = false,
                        encrypted = false,
                    )
                }
            }

            fun setLogging(isLoggingEnabled: Boolean) {
                if (isLoggingEnabled) {
                    logger.setMinLogLevel(libcdocLogLevel)
                    CDoc.setLogger(logger)
                    CDoc.log(LogLevel.LEVEL_DEBUG, "CryptoContainer", 450, "Set libcdoc logging: true")
                }
            }

            class JavaLogger : Logger() {
                override fun logMessage(
                    level: LogLevel?,
                    file: String?,
                    line: Int,
                    message: String?,
                ) {
                    debugLog("CryptoContainer", String.format("%s:%s %s %s\n", file, line, level, message))
                }
            }

            private class CryptoLibConf(
                private val cdoc2Settings: CDOC2Settings,
            ) : Configuration() {
                override fun getValue(
                    domain: String,
                    param: String,
                ): String =
                    when (param) {
                        KEYSERVER_FETCH_URL -> cdoc2Settings.getCDOC2FetchURL(domain)
                        KEYSERVER_SEND_URL -> cdoc2Settings.getCDOC2PostURL(domain)
                        else -> ""
                    }
            }

            private open class Network(
                private val cdoc2Settings: CDOC2Settings,
                private val configurationProvider: ConfigurationProvider?,
                private val context: Context,
            ) : NetworkBackend() {
                override fun getPeerTLSCertificates(
                    dst: CertificateList?,
                    url: String?,
                ): Long {
                    val certs = configurationProvider?.certBundle ?: listOf()
                    dst?.clear()
                    for (cert in certs) {
                        val certBytes = Base64.getDecoder().decode(cert)
                        dst?.addCertificate(certBytes)
                    }
                    val certFromSettings =
                        if (cdoc2Settings.isManualKeyServerUrl(url)) cdoc2Settings.getCDOC2Cert() else null
                    if (certFromSettings != null) {
                        try {
                            dst?.addCertificate(Base64.getDecoder().decode(certFromSettings))
                        } catch (e: IllegalArgumentException) {
                            errorLog(LOG_TAG, "Failed to decode manual key-server certificate", e)
                        }
                    }

                    return CDoc.OK.toLong()
                }

                override fun getProxyCredentials(cred: ProxyCredentials?): Long {
                    val proxyValues =
                        ProxyUtil.getProxyValues(
                            ProxyUtil.getProxySetting(context),
                            ProxyUtil.getManualProxySettings(context),
                        )

                    if (proxyValues != null) {
                        cred?.host = proxyValues.host
                        cred?.port = proxyValues.port
                        cred?.username = proxyValues.username
                        cred?.password = proxyValues.password
                    }
                    return CDoc.OK.toLong()
                }
            }

            private class CryptoLibNetworkBackend(
                cdoc2Settings: CDOC2Settings,
                configurationProvider: ConfigurationProvider?,
                context: Context,
                private var cert: ByteArray,
                private var token: SmartCardTokenWrapper,
            ) : Network(cdoc2Settings, configurationProvider, context) {
                override fun getClientTLSCertificate(dst: DataBuffer?): Long {
                    dst?.data = cert
                    return if (dst == null || dst.data == null || dst.data.isEmpty()) {
                        CDoc.IO_ERROR.toLong()
                    } else {
                        CDoc.OK.toLong()
                    }
                }

                override fun signTLS(
                    dst: DataBuffer,
                    algorithm: CryptoBackend.HashAlgorithm,
                    digest: ByteArray,
                ): Long = token.sign(dst, algorithm, digest, 0)
            }

            private class PasswordCryptoBackend(
                private val password: ByteArray,
            ) : CryptoBackend() {
                override fun getSecret(
                    dst: DataBuffer,
                    idx: Int,
                ): Long {
                    dst.data = password
                    return CDoc.OK.toLong()
                }
            }
        }
    }
