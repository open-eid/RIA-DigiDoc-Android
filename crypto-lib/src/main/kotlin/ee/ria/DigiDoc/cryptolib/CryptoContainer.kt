@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.common.Constant.CDOC1_EXTENSION
import ee.ria.DigiDoc.common.Constant.CDOC2_EXTENSION
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.cryptolib.exception.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.CryptoException
import ee.ria.DigiDoc.cryptolib.exception.DataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.RecipientsEmptyException
import ee.ria.DigiDoc.idcard.Token
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
import ee.ria.cdoc.Configuration
import ee.ria.cdoc.CryptoBackend
import ee.ria.cdoc.DataBuffer
import ee.ria.cdoc.FileInfo
import ee.ria.cdoc.Lock
import ee.ria.cdoc.NetworkBackend
import ee.ria.cdoc.Recipient
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
import javax.inject.Inject

private const val LOG_TAG = "CryptoContainer"

class CryptoContainer
    @Inject
    constructor(
        val context: Context,
        var file: File?,
        val dataFiles: ArrayList<File?>?,
        val recipients: ArrayList<Addressee?>?,
        val decrypted: Boolean,
        val encrypted: Boolean,
    ) {
        fun containerMimetype(): String = CONTAINER_MIME_TYPE

        fun getName(): String = file?.name ?: DEFAULT_FILENAME

        suspend fun setName(filename: String) {
            val name = sanitizeString(filename, "")
            val containerName = name?.let { ContainerUtil.addExtensionToContainerFilename(it) }
            val newFile = containerName?.let { File(file?.parent, it) }

            if (newFile != null) {
                withContext(IO) {
                    file?.renameTo(newFile)
                    file = newFile
                }
            }
        }

        fun addDataFiles(filesToAdd: List<File>) {
            dataFiles?.addAll(filesToAdd)
        }

        fun addRecipients(recipientsToAdd: List<Addressee>) {
            recipients?.addAll(recipientsToAdd)
        }

        fun getDataFiles(): List<File> =
            try {
                dataFiles
                    ?.filterNotNull()
                    ?.map { dataFile -> dataFile } ?: emptyList()
            } catch (e: Exception) {
                errorLog(LOG_TAG, "Unable to get container recipients", e)
                emptyList()
            }

        fun getRecipients(): List<Addressee> =
            try {
                recipients
                    ?.filterNotNull()
                    ?.map { recipient -> recipient } ?: emptyList()
            } catch (e: Exception) {
                errorLog(LOG_TAG, "Unable to get container recipients", e)
                emptyList()
            }

        fun hasRecipients(): Boolean = recipients?.isNotEmpty() == true

        @Throws(Exception::class)
        fun getDataFile(
            dataFile: File,
            directory: File?,
        ): File? {
            val file = sanitizeString(dataFile.name, "")?.let { File(directory, it) }
            val dataFiles = dataFiles
            if (dataFiles != null) {
                for (i in dataFiles.indices) {
                    val containerDataFile = dataFiles[i]
                    if (containerDataFile != null) {
                        if (dataFile.name == containerDataFile.name) {
                            if (file != null && !file.exists()) {
                                containerDataFile.saveAs(file.absolutePath)
                            }
                            return file
                        }
                    }
                }
            }
            throw IllegalArgumentException("Could not find file ${dataFile.name} in container ${file?.name}")
        }

        @Throws(Exception::class)
        suspend fun removeDataFile(dataFile: File) {
            if ((dataFiles?.size ?: 0) == 1) {
                throw ContainerDataFilesEmptyException()
            }

            val files = getDataFiles()
            withContext(IO) {
                for (i in files.indices) {
                    if (dataFile.name == files[i].name) {
                        dataFiles?.removeAt(i)
                        break
                    }
                }
            }
        }

        @Throws(Exception::class)
        fun removeRecipient(recipient: Addressee) {
            val signatures = getRecipients()
            if (signatures.isNotEmpty()) {
                for (i in signatures.indices) {
                    if (recipient.identifier == signatures[i].identifier) {
                        recipients?.removeAt(i)
                        break
                    }
                }
            }
        }

        companion object {
            @Throws(CryptoException::class)
            private suspend fun open(
                context: Context,
                file: File?,
            ): CryptoContainer {
                if (file?.extension == CDOC1_EXTENSION) {
                    return openCDOC1(context, file)
                }

                val addressees = ArrayList<Addressee>()
                val cdocReader = CDocReader.createReader(file?.path, null, null, null)
                debugLog(LOG_TAG, "Reader created: (version ${cdocReader.version})")

                withContext(IO) {
                    cdocReader.locks.forEach { lock ->
                        if (lock.isCertificate) {
                            addressees.add(Addressee(lock.label, lock.getBytes(Lock.Params.CERT)))
                        } else if (lock.isPKI) {
                            addressees.add(
                                Addressee(lock.label, lock.getBytes(Lock.Params.RCPT_KEY)),
                            )
                        } else {
                            addressees.add(Addressee("Unknown capsule", ByteArray(0)))
                        }
                    }
                    cdocReader.delete()
                }

                return create(context, file, listOf(), addressees, false, true)
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

                return create(context, file, dataFiles, recipients, false, true)
            }

            @Throws(CryptoException::class)
            fun decrypt(
                context: Context,
                file: File?,
                recipients: List<Addressee?>?,
                signerCert: ByteArray?,
                pin: ByteArray,
                smartToken: Token,
                cdoc2Settings: CDOC2Settings,
            ): CryptoContainer {
                val token = SmartCardTokenWrapper(pin, smartToken)
                val conf = CryptoLibConf(cdoc2Settings)
                val network = CryptoLibNetworkBackend()
                network.token = token
                if (signerCert != null) {
                    network.cert = signerCert
                }

                if (network.cert.isEmpty()) {
                    if (token.getLastError() != null) {
                        throw token.getLastError()!!
                    }
                }
                val dataFiles = ArrayList<File>()
                val cdocReader = CDocReader.createReader(file?.path, conf, token, network)
                val idx = cdocReader.getLockForCert(signerCert)
                if (idx < 0) {
                    throw CryptoException("Failed to get lock for certificate")
                }

                val fmk = cdocReader.getFMK(idx.toInt())

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
                        val fileToSave = sanitizeString(ofile.name, "")?.let { File(dir, it) }
                        val ofs: OutputStream = FileOutputStream(fileToSave)
                        cdocReader.readFile(ofs)
                        if (fileToSave != null) {
                            dataFiles.add(fileToSave)
                            ofs.close()
                        }
                        result = cdocReader.nextFile(fi)
                    }
                } catch (exc: IOException) {
                    throw CryptoException("IO Exception: ${exc.message}", exc)
                }

                if (cdocReader.finishDecryption() != 0L) {
                    throw CryptoException("Failed to finish decryption")
                }

                return create(context, file, dataFiles, recipients, true, false)
            }

            @Throws(CryptoException::class)
            suspend fun encrypt(
                context: Context,
                file: File?,
                dataFiles: List<File?>?,
                recipients: List<Addressee?>?,
                cdoc2Settings: CDOC2Settings,
            ): CryptoContainer {
                if (dataFiles.isNullOrEmpty()) {
                    throw DataFilesEmptyException("Cannot create an empty crypto container")
                }

                if (recipients.isNullOrEmpty()) {
                    throw RecipientsEmptyException("Cannot create crypto container without recipients")
                }

                val conf = CryptoLibConf(cdoc2Settings)
                val network = NetworkBackend()

                val version =
                    if (file?.extension == CDOC2_EXTENSION) {
                        2
                    } else {
                        1
                    }

                val cdocWriter = CDocWriter.createWriter(version, file?.path, conf, null, network)
                withContext(IO) {
                    if (version == 2 && cdoc2Settings.getUseOnlineEncryption()) {
                        val serverId = cdoc2Settings.getCDOC2UUID()
                        recipients.forEach { addressee ->
                            val recipient = Recipient.makeEIDServer(addressee?.data, serverId)
                            if (cdocWriter.addRecipient(recipient) != 0L) {
                                throw CryptoException("Failed to add recipient")
                            }
                        }
                    } else {
                        recipients.forEach { addressee ->
                            val recipient = Recipient.makeEID(addressee?.data)
                            if (cdocWriter.addRecipient(recipient) != 0L) {
                                throw CryptoException("Failed to add recipient")
                            }
                        }
                    }
                }

                try {
                    if (cdocWriter.beginEncryption() != 0L) {
                        throw CryptoException("Failed to begin encryption")
                    }

                    withContext(IO) {
                        dataFiles.forEach { dataFile ->
                            val ifs: InputStream = FileInputStream(dataFile)
                            val bytes = ifs.readBytes()
                            if (cdocWriter.addFile(dataFile?.name, bytes.size.toLong()) != 0L) {
                                throw CryptoException("Failed to add file")
                            }

                            if (cdocWriter.writeData(bytes) != 0L) {
                                throw CryptoException("Failed to write data")
                            }
                        }
                    }

                    if (cdocWriter.finishEncryption() != 0L) {
                        throw CryptoException("Failed to finish encryption")
                    }
                } catch (exc: IOException) {
                    errorLog(LOG_TAG, "IO Exception: ${exc.message}", exc)
                    System.err.println("IO Exception: " + exc.message)
                } catch (exc: CDocException) {
                    errorLog(LOG_TAG, "CDoc Exception ${exc.code}: ${exc.message}", exc)
                }

                return open(context, file)
            }

            @Throws(CryptoException::class)
            private fun create(
                context: Context,
                file: File?,
                dataFiles: List<File?>?,
                recipients: List<Addressee?>?,
                decrypted: Boolean,
                encrypted: Boolean,
            ): CryptoContainer {
                return CryptoContainer(
                    context,
                    file,
                    dataFiles?.let { ArrayList(it) },
                    recipients?.let { ArrayList(it) },
                    decrypted,
                    encrypted,
                )
            }

            @Throws(Exception::class)
            suspend fun openOrCreate(
                @ApplicationContext context: Context,
                file: File,
                dataFiles: List<File?>?,
                cdoc2Settings: CDOC2Settings,
            ): CryptoContainer {
                if (dataFiles.isNullOrEmpty()) {
                    throw DataFilesEmptyException("Cannot create an empty crypto container")
                }

                val isFirstDataFileContainer =
                    dataFiles.firstOrNull()?.run {
                        isCryptoContainer()
                    } == true

                var containerFileWithExtension = file

                if ((!isFirstDataFileContainer) &&
                    !file.path.endsWith(".$CDOC1_EXTENSION") &&
                    !file.path.endsWith(".$CDOC2_EXTENSION")
                ) {
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

                    file.copyTo(containerFileWithExtension, true)
                }

                return if (dataFiles.size == 1 && isFirstDataFileContainer) {
                    open(context, containerFileWithExtension)
                } else {
                    create(context, containerFileWithExtension, dataFiles, listOf(), false, false)
                }
            }

            private class CryptoLibConf(private val cdoc2Settings: CDOC2Settings) : Configuration() {
                override fun getValue(
                    domain: String?,
                    param: String?,
                ): String {
                    return when (param) {
                        KEYSERVER_FETCH_URL -> cdoc2Settings.getCDOC2FetchURL()
                        KEYSERVER_SEND_URL -> cdoc2Settings.getCDOC2PostURL()
                        else -> ""
                    }
                }
            }

            private class CryptoLibNetworkBackend : NetworkBackend() {
                lateinit var cert: ByteArray
                lateinit var token: SmartCardTokenWrapper

                override fun getClientTLSCertificate(dst: DataBuffer?): Long {
                    dst?.setData(cert)
                    return CDoc.OK.toLong()
                }

                override fun signTLS(
                    dst: DataBuffer,
                    algorithm: CryptoBackend.HashAlgorithm,
                    digest: ByteArray,
                ): Long {
                    return token.sign(dst, algorithm, digest, 0).toLong()
                }
            }
        }
    }
