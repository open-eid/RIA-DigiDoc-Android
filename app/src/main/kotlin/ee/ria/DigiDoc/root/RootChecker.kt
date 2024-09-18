@file:Suppress("PackageName")

package ee.ria.DigiDoc.root

import java.io.File

interface RootChecker {
    fun isRooted(): Boolean

    fun isRootRelatedDirectory(directory: File): Boolean
}
