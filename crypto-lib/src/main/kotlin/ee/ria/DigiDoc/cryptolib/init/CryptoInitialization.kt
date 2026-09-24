// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.init

import ee.ria.DigiDoc.cryptolib.CryptoContainer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoInitialization
    @Inject
    constructor() {
        /**
         * Initialize libcdoc-lib.
         */
        fun init(isLoggingEnabled: Boolean = false) {
            initNativeLibs()
            setLibCdocLogLevel(isLoggingEnabled)
        }

        private fun initNativeLibs() {
            System.loadLibrary("cdoc_java")
        }

        private fun setLibCdocLogLevel(isLoggingEnabled: Boolean) {
            CryptoContainer.setLogging(isLoggingEnabled)
        }
    }
