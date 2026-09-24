// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.locale

import android.content.Context
import java.util.Locale

interface LocaleUtil {
    fun getPreferredLanguage(context: Context?): String

    fun updateLocale(
        context: Context,
        locale: Locale,
    ): Context
}
