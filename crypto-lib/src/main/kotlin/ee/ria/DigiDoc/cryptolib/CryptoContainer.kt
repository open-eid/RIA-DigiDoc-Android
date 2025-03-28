@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import android.content.Context
import ee.ria.DigiDoc.common.Constant.CDOC1_EXTENSION
import ee.ria.DigiDoc.common.Constant.CDOC2_EXTENSION
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.cryptolib.exception.CryptoException
import ee.ria.DigiDoc.cryptolib.exception.DataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.RecipientsEmptyException
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getNameWithoutExtension
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
import org.openeid.cdoc4j.CDOCParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import javax.inject.Inject

private const val LOG_TAG = "CryptoContainer"

class CryptoContainer
    @Inject
    constructor(
        private val context: Context,
        private val file: File,
        private val dataFiles: List<File?>?,
        private val recipients: List<Addressee?>?,
        private val decrypted: Boolean,
    ) {
        companion object {
            @Throws(CryptoException::class)
            private suspend fun open(
                context: Context,
                file: File,
            ): CryptoContainer {
                if (file.extension === CDOC1_EXTENSION) {
                    return openCDOC1(context, file)
                }

                val addressees = ArrayList<Addressee>()
                val cdocReader = CDocReader.createReader(file.path, null, null, null)
                debugLog(LOG_TAG, "Reader created: (version ${cdocReader.version})")

                withContext(IO) {
                    cdocReader.locks.forEach { lock ->
                        if (lock.isCertificate) {
                            addressees.add(Addressee(lock.label, lock.getBytes(Lock.Params.CERT)))
                        } else if (lock.isPKI) {
                            addressees.add(Addressee(lock.label, lock.getBytes(Lock.Params.RCPT_KEY)))
                        } else {
                            addressees.add(Addressee("Unknown capsule", ByteArray(0)))
                        }
                    }
                }

                return create(context, file, null, addressees, false)
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

                return create(context, file, dataFiles, recipients, false)
            }

            @Throws(CryptoException::class)
            private suspend fun decrypt(
                context: Context,
                file: File,
                smartToken: AbstractSmartToken,
                cdoc2Settings: CDOC2Settings,
            ): CryptoContainer {
                val token = SmartCardTokenWrapper(smartToken)
                val conf = CryptoLibConf(cdoc2Settings)
                val network = CryptoLibNetworkBackend()
                network.token = token
                network.cert = token.cert()

                if (network.cert.isEmpty()) {
                    if (token.getLastError() != null) {
                        throw token.getLastError()!!
                    }
                }
                val dataFiles = ArrayList<File>()
                val cdocReader = CDocReader.createReader(file.path, conf, null, network)
                val idx = cdocReader.getLockForCert(network.cert)

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
                withContext(IO) {
                    val fi = FileInfo()
                    var result: Long = cdocReader.nextFile(fi)
                    try {
                        while (result == CDoc.OK.toLong()) {
                            val ofile = File(fi.name)
                            val ofs: OutputStream = FileOutputStream(ofile.name)
                            cdocReader.readFile(ofs)
                            dataFiles.add(ofile)
                            result = cdocReader.nextFile(fi)
                        }
                    } catch (exc: IOException) {
                        throw CryptoException("IO Exception: ${exc.message}", exc)
                    }
                }
                if (cdocReader.finishDecryption() != 0L) {
                    throw CryptoException("Failed to finish decryption")
                }
                val cryptoContainer = open(context, file)
                return create(context, file, dataFiles, cryptoContainer.recipients, true)
            }

            @Throws(CryptoException::class)
            private suspend fun encrypt(
                context: Context,
                file: File,
                dataFiles: List<File?>?,
                recipients: List<Addressee?>?,
                cdoc2Settings: CDOC2Settings,
            ): CryptoContainer {
                val conf = CryptoLibConf(cdoc2Settings)
                val network = NetworkBackend()

                val version =
                    if (file.extension === CDOC2_EXTENSION) {
                        2
                    } else {
                        1
                    }

                val cdocWriter = CDocWriter.createWriter(version, file.path, conf, null, network)

                if (version == 2 && cdoc2Settings.getUseOnlineEncryption()) {
                    val serverId = cdoc2Settings.getCDOC2SelectedService()
                    recipients?.forEach { addressee ->
                        val recipient = Recipient.makeEIDServer(addressee?.data, serverId)
                        if (cdocWriter.addRecipient(recipient) != 0L) {
                            throw CryptoException("Failed to add recipient")
                        }
                    }
                } else {
                    recipients?.forEach { addressee ->
                        val recipient = Recipient.makeEID(addressee?.data)
                        if (cdocWriter.addRecipient(recipient) != 0L) {
                            throw CryptoException("Failed to add recipient")
                        }
                    }
                }

                try {
                    if (cdocWriter.beginEncryption() != 0L) {
                        throw CryptoException("Failed to begin encryption")
                    }

                    withContext(IO) {
                        dataFiles?.forEach { dataFile ->
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
                file: File,
                dataFiles: List<File?>?,
                recipients: List<Addressee?>?,
                decrypted: Boolean,
            ): CryptoContainer {
                if (dataFiles.isNullOrEmpty()) {
                    throw DataFilesEmptyException("Cannot create an empty crypto container")
                }

                if (recipients.isNullOrEmpty()) {
                    throw RecipientsEmptyException("Cannot create crypto container without recipients")
                }

                return CryptoContainer(context, file, dataFiles, recipients, decrypted)
            }

            fun createCDOC1ContainerFileName(file: String): String {
                val fileName: String? = getNameWithoutExtension(file)
                return "$fileName.$CDOC1_EXTENSION"
            }

            fun createCDOC2ContainerFileName(file: String): String {
                val fileName: String? = getNameWithoutExtension(file)
                return "$fileName.$CDOC2_EXTENSION"
            }

            fun isCryptoContainer(file: File): Boolean {
                val extension: String = file.extension.lowercase(Locale.getDefault())
                return CDOC1_EXTENSION == extension || CDOC2_EXTENSION == extension
            }

            fun getMimeType(): String {
                return CONTAINER_MIME_TYPE
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
                    dst?.data = cert
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
