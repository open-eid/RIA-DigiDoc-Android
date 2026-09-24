// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ee.ria.DigiDoc.init.EncryptedPreferencesMigration

@HiltAndroidApp
class RIADigiDocApp : Application() {
    override fun onCreate() {
        super.onCreate()
        EncryptedPreferencesMigration.migrate(this)
    }
}
