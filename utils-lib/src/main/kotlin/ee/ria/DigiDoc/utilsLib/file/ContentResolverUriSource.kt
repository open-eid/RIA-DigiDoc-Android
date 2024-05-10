@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.file

import android.content.ContentResolver
import android.net.Uri
import com.google.common.io.ByteSource
import java.io.IOException
import java.io.InputStream

internal class ContentResolverUriSource(
    private val contentResolver: ContentResolver?,
    private val uri: Uri?,
) : ByteSource() {
    @Throws(IOException::class)
    override fun openStream(): InputStream {
        return uri?.let { contentResolver?.openInputStream(it) }!!
    }
}
