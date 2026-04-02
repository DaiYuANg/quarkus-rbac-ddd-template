import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskAction

abstract class VerifyLayerDependenciesTask : DefaultTask() {
  private val dependencyConfigurations = setOf("api", "implementation", "compileOnly", "runtimeOnly")
  private val allowedModuleDependencies =
      mapOf(":modules:security-runtime" to setOf(":modules:identity"))
  private val allowedAppLibraryDependencies =
      mapOf(
          ":apps:admin-api" to
              setOf(
                  ":libs:common",
                  ":libs:rest-support",
                  ":libs:security",
                  ":libs:identity",
                  ":libs:accesscontrol"),
          ":apps:mobile-api" to
              setOf(":libs:common", ":libs:rest-support", ":libs:security"),
          ":apps:migrator" to emptySet())

  init {
    group = "verification"
    description = "Verify Gradle layer rules and keep HTTP query params out of shared read models"
  }

  @TaskAction
  fun verify() {
    val violations = mutableListOf<String>()
    val subprojects = project.rootProject.subprojects

    subprojects.forEach { candidate ->
      candidate.configurations
          .filter { it.name in dependencyConfigurations }
          .forEach { configuration ->
            configuration.dependencies.withType(ProjectDependency::class.java).forEach { dependency ->
              val dependencyPath = dependency.path
              when {
                candidate.path.startsWith(":libs:") &&
                    (dependencyPath.startsWith(":modules:") || dependencyPath.startsWith(":apps:")) ->
                    violations +=
                        "${candidate.path} -> ${dependencyPath} (configuration: ${configuration.name})"
                candidate.path.startsWith(":modules:") && dependencyPath.startsWith(":apps:") ->
                    violations +=
                        "${candidate.path} -> ${dependencyPath} (configuration: ${configuration.name})"
                candidate.path.startsWith(":modules:") && dependencyPath.startsWith(":modules:") -> {
                  val allowlist = allowedModuleDependencies[candidate.path].orEmpty()
                  if (dependencyPath !in allowlist) {
                    violations +=
                        "${candidate.path} -> ${dependencyPath} (configuration: ${configuration.name})"
                  }
                }
                candidate.path.startsWith(":apps:") && dependencyPath.startsWith(":libs:") -> {
                  val allowlist = allowedAppLibraryDependencies[candidate.path].orEmpty()
                  if (dependencyPath !in allowlist) {
                    violations +=
                        "${candidate.path} -> ${dependencyPath} (configuration: ${configuration.name})"
                  }
                }
                candidate.path.startsWith(":apps:") && dependencyPath.startsWith(":apps:") ->
                    violations +=
                        "${candidate.path} -> ${dependencyPath} (configuration: ${configuration.name})"
              }
            }
          }
    }

    verifySharedQueryModelsAreHttpFree(subprojects, violations)
    verifyExampleReferenceLayout(subprojects, violations)

    if (violations.isNotEmpty()) {
      throw GradleException(
          buildString {
            appendLine("Architecture guardrails violated.")
            appendLine("Found the following forbidden dependencies or adapter leaks:")
            violations.sorted().forEach { appendLine(" - $it") }
          })
    }
  }

  private fun verifySharedQueryModelsAreHttpFree(
      subprojects: Iterable<org.gradle.api.Project>,
      violations: MutableList<String>,
  ) {
    subprojects
        .filter { it.path in setOf(":libs:common", ":libs:identity", ":libs:accesscontrol") }
        .forEach { candidate ->
          val javaSources = candidate.fileTree(candidate.projectDir.resolve("src/main/java")) {
            include("**/*.java")
          }
          javaSources.forEach { source ->
            val content = source.readText()
            if (
                content.contains("import jakarta.ws.rs.QueryParam;") ||
                    content.contains("@QueryParam(")
            ) {
              val relativePath = source.relativeTo(project.rootProject.projectDir).invariantSeparatorsPath
              violations +=
                  "${candidate.path} -> $relativePath uses @QueryParam outside the adapter layer"
            }
          }
        }
  }

  private fun verifyExampleReferenceLayout(
      subprojects: Iterable<org.gradle.api.Project>,
      violations: MutableList<String>,
  ) {
    val exampleProject = subprojects.firstOrNull { it.path == ":modules:example-ddd" } ?: return
    val javaSources = exampleProject.fileTree(exampleProject.projectDir.resolve("src/main/java")) {
      include("**/*.java")
    }
    javaSources.forEach { source ->
      val relativePath = source.relativeTo(project.rootProject.projectDir).invariantSeparatorsPath
      val content = source.readText()
      when {
        relativePath.contains("/application/command/") &&
            (content.contains("import jakarta.persistence.") ||
                content.contains("import jakarta.ws.rs.")) ->
            violations +=
                ":modules:example-ddd -> $relativePath leaks persistence/HTTP annotations into application commands"
        relativePath.contains("/application/readmodel/") &&
            (content.contains("import jakarta.persistence.") ||
                content.contains("import jakarta.ws.rs.") ||
                content.contains("@QueryParam(")) ->
            violations +=
                ":modules:example-ddd -> $relativePath leaks persistence/HTTP annotations into read models"
        relativePath.contains("/domain/") &&
            (content.contains("import jakarta.ws.rs.") ||
                content.contains("import io.quarkus.") ||
                content.contains("import jakarta.inject.") ||
                content.contains("import jakarta.enterprise.context.")) ->
            violations +=
                ":modules:example-ddd -> $relativePath leaks adapter/runtime annotations into domain code"
      }
    }
  }
}
