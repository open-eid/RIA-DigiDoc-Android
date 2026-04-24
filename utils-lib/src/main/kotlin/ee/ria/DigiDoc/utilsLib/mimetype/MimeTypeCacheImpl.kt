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

package ee.ria.DigiDoc.utilsLib.mimetype

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.utilsLib.extensions.md5Hash
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.model.MimeTypeCacheEntry
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MimeTypeCacheImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : MimeTypeCache {
        private val cache = mutableMapOf<String, MimeTypeCacheEntry>()

        override fun getMimeType(file: File): String {
            val md5 = file.md5Hash()

            return cache[md5]?.mimeType ?: run {
                val mimeType = file.mimeType(context)
                setMimeType(md5, mimeType)
                mimeType
            }
        }

        override fun setMimeType(
            md5: String,
            mimeType: String,
        ) {
            cache[md5] = MimeTypeCacheEntry(mimeType)
        }
    }
