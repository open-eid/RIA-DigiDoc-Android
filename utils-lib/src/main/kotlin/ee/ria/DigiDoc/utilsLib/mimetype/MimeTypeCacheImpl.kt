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
