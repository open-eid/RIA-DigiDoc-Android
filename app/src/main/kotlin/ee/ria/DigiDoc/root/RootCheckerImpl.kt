@file:Suppress("PackageName")

package ee.ria.DigiDoc.root

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootCheckerImpl
    @Inject
    constructor(
        private val rootRelatedDirectories: List<File>,
    ) : RootChecker {
        constructor() : this(
            listOf(
                File("/sbin"),
                File("/su/bin"),
                File("/system/bin/su"),
            ),
        )

        override fun isRooted(): Boolean = deviceHasRootRelatedDirectories(rootRelatedDirectories)

        override fun isRootRelatedDirectory(directory: File): Boolean = directory.exists()

        private fun deviceHasRootRelatedDirectories(rootedDirectories: List<File>): Boolean =
            rootedDirectories.any { dir -> isRootRelatedDirectory(dir) }
    }
