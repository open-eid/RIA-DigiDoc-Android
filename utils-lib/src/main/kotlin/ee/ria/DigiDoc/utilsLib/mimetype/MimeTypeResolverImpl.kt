@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.mimetype

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.utilsLib.extensions.md5Hash
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MimeTypeResolverImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val mimeTypeCache: MimeTypeCache,
    ) : MimeTypeResolver {
        override fun mimeType(file: File?): String =
            file?.let {
                mimeTypeCache.getMimeType(it.path).takeIf { mimetype -> mimetype.isNotEmpty() }
                    ?: it.mimeType(context).also { mimeType ->
                        mimeTypeCache.setMimeType(it.md5Hash(), mimeType)
                    }
            } ?: DEFAULT_MIME_TYPE
    }
