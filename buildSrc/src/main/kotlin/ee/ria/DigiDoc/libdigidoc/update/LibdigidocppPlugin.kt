@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoc.update

import org.gradle.api.Plugin
import org.gradle.api.Project

open class LibdigidocppPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("updateLibdigidocpp", UpdateLibdigidocppTask::class.java)
    }
}
