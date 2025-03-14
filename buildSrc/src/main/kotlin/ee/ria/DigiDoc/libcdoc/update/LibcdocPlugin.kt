@file:Suppress("PackageName")

package ee.ria.DigiDoc.libcdoc.update

import org.gradle.api.Plugin
import org.gradle.api.Project

open class LibcdocPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("updateLibcdoc", UpdateLibcdocTask::class.java)
    }
}
