// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.init

import android.content.Context
import android.system.ErrnoException
import android.system.Os
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibdigidocLibraryLoader
    @Inject
    constructor() {
        private val libdigidocInitLogTag = "LibdigidocLibraryLoader"
        private var isLoaded = false

        @Throws(ErrnoException::class)
        @Synchronized
        fun init(context: Context) {
            if (isLoaded) return

            initHomeDir(context)
            initNativeLibs()
            debugLog(libdigidocInitLogTag, "Libdigidocpp loaded")
            isLoaded = true
        }

        private fun initNativeLibs() {
            System.loadLibrary("digidoc_java")
        }

        @Throws(ErrnoException::class)
        fun initHomeDir(context: Context) {
            val path: String = FileUtils.getSchemaPath(context)
            try {
                Os.setenv("HOME", path, true)
            } catch (erre: ErrnoException) {
                errorLog(
                    libdigidocInitLogTag,
                    "Setting HOME environment variable failed: ${erre.message}",
                )
                throw erre
            }
        }
    }
