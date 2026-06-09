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
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.network.utils.ProxyUtil
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.extensions.isCryptoContainer
import ee.ria.DigiDoc.utilsLib.extensions.saveAs
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
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
import ee.ria.cdoc.ILogger
import ee.ria.cdoc.Lock
import ee.ria.cdoc.NetworkBackend
import ee.ria.cdoc.Recipient
import ee.ria.cdoc.Recipient.parseLabel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import org.openeid.cdoc4j.CDOCParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

private const val LOG_TAG = "CryptoContainer"

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
            var loggingIsSet = false

            @Throws(CryptoException::class)
            private suspend fun open(
                context: Context,
                file: File,
            ): CryptoContainer {
                val cdoc1 = if (file.extension == CDOC1_EXTENSION) openCDOC1(context, file) else null

                val cdocReader = CDocReader.createReader(file.path, null, null, null)
                debugLog(LOG_TAG, "Reader created: (version ${cdocReader.version})")
                val lockAddressees =
                    withContext(IO) {
                        try {
                            cdocReader.locks.map(::addresseeOf)
                        } finally {
                            cdocReader.delete()
                        }
                    }

                val cdoc1Recipients = cdoc1?.getRecipients().orEmpty()
                val recipients =
                    if (cdoc1Recipients.isNotEmpty()) {
                        cdoc1Recipients.onEach { recipient ->
                            lockAddressees
                                .firstOrNull { it.data.contentEquals(recipient.data) }
                                ?.let { recipient.concatKDFAlgorithmURI = it.concatKDFAlgorithmURI }
                        }
                    } else {
                        lockAddressees
                    }

                return create(
                    context,
                    file,
                    cdoc1?.getDataFiles().orEmpty(),
                    recipients,
                    decrypted = false,
                    encrypted = true,
                )
            }

            private fun addresseeOf(lock: Lock): Addressee =
                when {
                    lock.isCDoc1 ->
                        Addressee(lock.getBytes(Lock.Params.CERT)).apply {
                            if (!lock.isRSA) {
                                concatKDFAlgorithmURI = lock.getString(Lock.Params.CONCAT_DIGEST)
                            }
                        }
                    lock.isPKI -> Addressee(lock.label, lock.getBytes(Lock.Params.RCPT_KEY), "")
                    lock.isSymmetric -> Addressee(lock.label, "", CertType.UnknownType, null, ByteArray(0))
                    else -> Addressee("Unknown capsule", ByteArray(0), "")
                }.apply {
                    keyLabel = lock.label.takeIf { it.isNotBlank() }
                    if (lock.type == Lock.Type.SERVER) {
                        serverId = lock.getString(Lock.Params.KEYSERVER_ID).takeIf { it.isNotBlank() }
                        transactionId = lock.getString(Lock.Params.TRANSACTION_ID).takeIf { it.isNotBlank() }
                    }
                }

            @Throws(CryptoException::class)
            suspend fun openCDOC1(
                context: Context,
                file: File,
            ): CryptoContainer {
                val dataFiles = ArrayList<File>()
                val recipients = ArrayList<Addressee>()

                withContext(IO) {
                    try {
                        FileInputStream(file).use { dataFilesStream ->
                            CDOCParser.getDataFileNames(dataFilesStream).forEach { dataFileName ->
                                dataFiles.add(File(dataFileName))
                            }
                        }
                        FileInputStream(file).use { recipientsStream ->
                            CDOCParser.getRecipients(recipientsStream).forEach { recipient ->
                                val addressee = Addressee(recipient.certificate.encoded)
                                recipients.add(addressee)
                            }
                        }
                    } catch (e: Exception) {
                        throw CryptoException("Can't open crypto container", e)
                    }
                }

                return create(
                    context,
                    file,
                    dataFiles,
                    recipients,
                    decrypted = false,
                    encrypted = true,
                )
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
            ): CryptoContainer {
                val token = SmartCardTokenWrapper(pin, smartToken)
                val conf = CryptoLibConf(cdoc2Settings)
                val configurationProvider = configurationRepository.getConfiguration()

                if (authCert == null || authCert.isEmpty()) {
                    throw CryptoException("Failed to get auth certificate")
                }
                val network = CryptoLibNetworkBackend(cdoc2Settings, configurationProvider, context, authCert, token)
                val dataFiles = ArrayList<File>()

                val cdocReader = CDocReader.createReader(file.path, conf, token, network)
                debugLog(LOG_TAG, "Reader created: (version ${cdocReader.version})")
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

                val fi = FileInfo()
                var result: Long = cdocReader.nextFile(fi)
                try {
                    while (result == CDoc.OK.toLong()) {
                        val ofile = File(fi.name)
                        val dir =
                            ContainerUtil.getContainerDataFilesDir(
                                context,
                                file,
                            )
                        val tmp = sanitizeString(ofile.name, "")
                        val fileToSave = File(dir, tmp)
                        val ofs: OutputStream = FileOutputStream(fileToSave)
                        cdocReader.readFile(ofs)
                        dataFiles.add(fileToSave)
                        ofs.close()
                        result = cdocReader.nextFile(fi)
                    }
                } catch (exc: IOException) {
                    throw CryptoException("IO Exception: ${exc.message}", exc)
                }

                if (cdocReader.finishDecryption() != 0L) {
                    throw CryptoException("Failed to finish decryption")
                }

                return create(
                    context,
                    file,
                    dataFiles,
                    recipients,
                    decrypted = true,
                    encrypted = false,
                )
            }

            @Throws(CryptoException::class)
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

                val cdocWriter = CDocWriter.createWriter(version, file.path, conf, null, network)
                try {
                    withContext(IO) {
                        if (version == 2 && cdoc2Settings.getUseOnlineEncryption()) {
                            val serverId = cdoc2Settings.getCDOC2UUID()
                            recipients.forEach { addressee ->
                                val recipient = Recipient.makeServer("", addressee.data, serverId)
                                if (cdocWriter.addRecipient(recipient) != 0L) {
                                    throw CryptoException("Failed to add recipient")
                                }
                            }
                        } else {
                            recipients.forEach { addressee ->
                                val recipient = Recipient.makeCertificate("", addressee.data)
                                if (cdocWriter.addRecipient(recipient) != 0L) {
                                    throw CryptoException("Failed to add recipient")
                                }
                            }
                        }
                    }
                    if (cdocWriter.beginEncryption() != 0L) {
                        throw CryptoException("Failed to begin encryption")
                    }
                    withContext(IO) {
                        dataFiles.forEach { dataFile ->
                            val ifs: InputStream = FileInputStream(dataFile)
                            val bytes = ifs.readBytes()
                            if (cdocWriter.addFile(dataFile.name, bytes.size.toLong()) != 0L) {
                                throw CryptoException("Failed to add file")
                            }

                            if (cdocWriter.writeData(bytes) != 0L) {
                                throw CryptoException("Failed to write data")
                            }
                            ifs.close()
                        }
                    }

                    if (cdocWriter.finishEncryption() != 0L) {
                        throw CryptoException("Failed to finish encryption")
                    }
                } catch (exc: IOException) {
                    errorLog(LOG_TAG, "IO Exception: ${exc.message}", exc)
                    throw CryptoException("IO Exception: ${exc.message}", exc)
                } catch (exc: CDocException) {
                    errorLog(LOG_TAG, "CDoc Exception ${exc.code}: ${exc.message}", exc)
                    throw CryptoException("CDoc Exception ${exc.code}: ${exc.message}", exc)
                } finally {
                    cdocWriter.delete()
                }

                return open(context, file)
            }

            @Throws(CryptoException::class)
            private fun create(
                context: Context,
                file: File,
                dataFiles: List<File>,
                recipients: List<Addressee>,
                decrypted: Boolean,
                encrypted: Boolean,
            ): CryptoContainer =
                CryptoContainer(
                    context,
                    file,
                    ArrayList(dataFiles),
                    ArrayList(recipients),
                    decrypted,
                    encrypted,
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
                    logger.SetMinLogLevel(ILogger.LogLevel.LEVEL_TRACE)
                    if (!loggingIsSet) {
                        ILogger.addLogger(logger)
                        loggingIsSet = true
                    }
                    val lgr = ILogger.getLogger()

                    lgr.LogMessage(
                        ILogger.LogLevel.LEVEL_DEBUG,
                        "CryptoContainer",
                        450,
                        "Set libcdoc logging: true",
                    )
                }
            }

            class JavaLogger : ILogger() {
                override fun LogMessage(
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
                    val certFromSettings = cdoc2Settings.getCDOC2Cert()
                    if (certFromSettings != null) {
                        val certBytes =
                            Base64.getDecoder().decode(certFromSettings)
                        dst?.addCertificate(certBytes)
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
        }
    }
