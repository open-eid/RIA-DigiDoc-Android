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
