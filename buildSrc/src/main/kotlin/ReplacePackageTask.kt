import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to batch-replace package names and group.
 *
 * Usage:
 *   ./gradlew replacePackage -PfromPackage=com.github.DaiYuANg -PtoPackage=com.example.myproject
 *
 * Optional dry-run (print only, no changes):
 *   ./gradlew replacePackage -PfromPackage=... -PtoPackage=... -PdryRun=true
 *
 * Performs:
 * 1. Replace package in all source files (java, kt, kts, yaml, md, sql, xml, properties)
 * 2. Update group in build.gradle.kts
 * 3. Rename package directories under source roots such as src/main/java, src/test/java,
 *    src/main/kotlin, and src/test/kotlin across apps/libs/modules
 */
abstract class ReplacePackageTask : DefaultTask() {

  @get:Input
  abstract val fromPackage: Property<String>

  @get:Input
  abstract val toPackage: Property<String>

  @get:Input
  @get:Optional
  abstract val dryRun: Property<Boolean>

  init {
    fromPackage.convention(project.provider { project.findProperty("fromPackage") as? String ?: "" })
    toPackage.convention(project.provider { project.findProperty("toPackage") as? String ?: "" })
    dryRun.convention(project.provider { (project.findProperty("dryRun") as? String)?.toBoolean() ?: false })
  }

  @TaskAction
  fun execute() {
    val from = fromPackage.get()
    val to = toPackage.get()
    val dry = dryRun.get()

    if (from.isBlank() || to.isBlank()) {
      logger.error("Usage: ./gradlew replacePackage -PfromPackage=<from> -PtoPackage=<to>")
      logger.error("  Example: ./gradlew replacePackage -PfromPackage=com.github.DaiYuANg -PtoPackage=com.example.myproject")
      throw IllegalStateException("fromPackage and toPackage are required")
    }

    if (from == to) {
      logger.lifecycle("Package names are identical, no replacement needed")
      return
    }

    val rootDir = project.rootProject.layout.projectDirectory.asFile

    logger.lifecycle("Replacing package: $from -> $to")
    if (dry) logger.lifecycle("(dry-run mode, no files will be modified)")

    // 1. Replace file contents
    var replaceCount = 0
    rootDir.walkTopDown()
      .filter { file -> ReplacePackageSupport.shouldReplaceContent(rootDir, file) }
      .forEach { file ->
        val content = file.readText(Charsets.UTF_8)
        val newContent = ReplacePackageSupport.replacePackageInContent(content, from, to)
        if (content != newContent) {
          if (!dry) file.writeText(newContent, Charsets.UTF_8)
          replaceCount++
          logger.lifecycle("  replaced: ${file.relativeTo(rootDir)}")
        }
      }

    logger.lifecycle("Content replaced in $replaceCount files")

    // 2. Update group in build.gradle.kts (when it matches from)
    val buildGradle = rootDir.resolve("build.gradle.kts")
    if (buildGradle.exists()) {
      var buildContent = buildGradle.readText(Charsets.UTF_8)
      val groupRegex = """group\s*=\s*["']([^"']+)["']""".toRegex()
      val match = groupRegex.find(buildContent)
      if (match != null && match.groupValues[1] == from) {
        buildContent = buildContent.replace(groupRegex) { "group = \"$to\"" }
        if (!dry) buildGradle.writeText(buildContent, Charsets.UTF_8)
        logger.lifecycle("  Updated build.gradle.kts group: $from -> $to")
      }
    }

    // 3. Rename directory structure
    val moveCount =
      ReplacePackageSupport.renamePackageDirectories(
        rootDir = rootDir,
        fromPackage = from,
        toPackage = to,
        dryRun = dry,
        log = logger::lifecycle,
      )
    if (from != to) {
      logger.lifecycle("Directories renamed: $moveCount")
    }

    logger.lifecycle("Done!")
  }
}
