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

package ee.ria.DigiDoc.ui.component.myeid.mydata

import android.content.Context
import ee.ria.DigiDoc.R

enum class MyEidDocumentStatus {
    VALID,
    EXPIRED,
    UNKNOWN,
}

fun MyEidDocumentStatus.getLocalized(context: Context): String =
    when (this) {
        MyEidDocumentStatus.VALID -> context.getString(R.string.myeid_status_valid)
        MyEidDocumentStatus.EXPIRED -> context.getString(R.string.myeid_status_expired)
        MyEidDocumentStatus.UNKNOWN -> context.getString(R.string.myeid_status_unknown)
    }
