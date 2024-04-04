package ee.ria.DigiDoc.libdigidoclib.update

import org.gradle.api.Plugin
import org.gradle.api.Project

open class LibdigidocppPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("updateLibdigidocpp", UpdateLibdigidocppTask::class.java)
    }
}
