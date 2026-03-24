import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskAction

abstract class VerifyLayerDependenciesTask : DefaultTask() {
  init {
    group = "verification"
    description = "Ensure libs modules do not depend on business modules"
  }

  @TaskAction
  fun verify() {
    val violations = mutableListOf<String>()
    project.rootProject.subprojects
        .filter { it.path.startsWith(":libs:") }
        .forEach { candidate ->
          candidate.configurations
              .filter { it.name in setOf("api", "implementation", "compileOnly", "runtimeOnly") }
              .forEach { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java).forEach {
                    dependency ->
                  val dependencyPath = dependency.path
                  if (dependencyPath.startsWith(":modules:")) {
                    violations +=
                        "${candidate.path} -> ${dependencyPath} (configuration: ${configuration.name})"
                  }
                }
              }
        }

    if (violations.isNotEmpty()) {
      throw GradleException(
          buildString {
            appendLine("Layer dependency rule violated: libs cannot depend on modules.")
            appendLine("Found the following forbidden dependencies:")
            violations.sorted().forEach { appendLine(" - $it") }
          })
    }
  }
}
