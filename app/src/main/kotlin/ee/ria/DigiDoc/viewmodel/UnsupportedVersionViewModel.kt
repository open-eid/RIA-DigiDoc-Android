// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.utils.VersionUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import javax.inject.Inject

@HiltViewModel
class UnsupportedVersionViewModel
    @Inject
    constructor(
        private val configurationLoader: ConfigurationLoader,
    ) : ViewModel() {
        private val logTag = "UnsupportedVersionViewModel"

        private var isUnsupported: Boolean? = null

        fun isAppVersionUnsupported(appVersion: String): Boolean {
            isUnsupported?.let { return it }

            val unsupportedVersion = configurationLoader.getConfigurationFlow().value?.riaddUnsupported
            val result = VersionUtil.isLowerThan(appVersion, unsupportedVersion)
            if (result) {
                errorLog(logTag, "App version $appVersion is unsupported")
            }
            isUnsupported = result
            return result
        }
    }
