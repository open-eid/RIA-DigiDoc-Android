@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.init

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.system.ErrnoException
import android.system.Os
import ee.ria.DigiDoc.libdigidoclib.BuildConfig
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaDir
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaPath
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.initSchema
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.libdigidocpp.Conf
import ee.ria.libdigidocpp.DigiDocConf
import ee.ria.libdigidocpp.digidoc
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

object Initialization {
    private var isInitialized = false
    private const val LIBDIGIDOC_INIT_LOG_TAG = "Libdigidoc-Initialization"
    private const val LIBDIGIDOC_LOG_LEVEL = 4 // 4 - Debug messages

    /**
     * Initialize libdigidoc-lib.
     *
     * Unzips the schema, access certificate and initializes libdigidocpp.
     */
    @Throws(IOException::class, NotFoundException::class)
    suspend fun init(context: Context) {
        if (isInitialized) {
            throw IllegalStateException("Libdigidocpp is already initialized")
        }
        coroutineScope {
            launch(IO) {
                initHomeDir(context)
                initNativeLibs()
                try {
                    initSchema(context)
                } catch (ioe: IOException) {
                    errorLog(LIBDIGIDOC_INIT_LOG_TAG, "Init schema failed: ${ioe.message}")
                    throw ioe
                } catch (nfe: NotFoundException) {
                    errorLog(LIBDIGIDOC_INIT_LOG_TAG, "Init schema failed: ${nfe.message}")
                    throw nfe
                } catch (erre: ErrnoException) {
                    errorLog(LIBDIGIDOC_INIT_LOG_TAG, "Init schema failed: ${erre.message}")
                    throw erre
                }

                initLibDigiDocpp(
                    context,
                    getSchemaPath(context),
                )
            }
        }
    }

    @Throws(ErrnoException::class)
    private fun initHomeDir(context: Context) {
        val path: String = getSchemaPath(context)
        try {
            Os.setenv("HOME", path, true)
        } catch (erre: ErrnoException) {
            errorLog(LIBDIGIDOC_INIT_LOG_TAG, "Setting HOME environment variable failed: ${erre.message}")
            throw erre
        }
    }

    private fun initNativeLibs() {
        System.loadLibrary("digidoc_java")
    }

    private fun initLibDigiDocpp(
        context: Context,
        path: String,
    ) {
        initLibDigiDocConfiguration(
            context,
        )
        digidoc.initializeLib("RIA DigiDoc", path)
        isInitialized = true
    }

    private fun initLibDigiDocConfiguration(context: Context) {
        val conf = DigiDocConf(getSchemaDir(context).absolutePath)
        Conf.init(conf.transfer())
        if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            initLibDigiDocLogging()
        }

        forcePKCS12Certificate()
    }

    private fun forcePKCS12Certificate() {
        DigiDocConf.instance().setPKCS12Cert("798.p12")
    }

    private fun initLibDigiDocLogging() {
        DigiDocConf.instance().setLogLevel(LIBDIGIDOC_LOG_LEVEL)
    }
}
