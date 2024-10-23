@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.mimetype

import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MimeTypeResolverImpl
    @Inject
    constructor(
        private val mimeTypeCache: MimeTypeCache,
    ) : MimeTypeResolver {
        override fun mimeType(file: File?): String? =
            file?.let {
                mimeTypeCache.getMimeType(it).takeIf { mimetype -> mimetype.isNotEmpty() }
                    ?: DEFAULT_MIME_TYPE
            }
    }
