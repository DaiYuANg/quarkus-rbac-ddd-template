import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

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
 * 3. Rename directory structure (com/github/DaiYuANg -> com/example/myproject)
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
    val extensions = listOf("java", "kt", "kts", "yaml", "yml", "md", "sql", "xml", "properties")
    val excludeDirs = setOf("build", ".gradle", ".git", "buildSrc/build", "buildSrc")

    logger.lifecycle("Replacing package: $from -> $to")
    if (dry) logger.lifecycle("(dry-run mode, no files will be modified)")

    // 1. Replace file contents
    var replaceCount = 0
    rootDir.walkTopDown()
      .filter { it.isFile }
      .filter { file ->
        val relativePath = file.relativeTo(rootDir).path
        !excludeDirs.any { relativePath.contains("/$it/") || relativePath.startsWith("$it/") }
      }
      .filter { extensions.any { ext -> it.extension.equals(ext, ignoreCase = true) } }
      .forEach { file ->
        val content = file.readText(Charsets.UTF_8)
        val newContent = content
          .replace(from, to)
          .replace(from.replace(".", "/"), to.replace(".", "/"))
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
    val fromPath = from.replace(".", File.separator)
    val toPath = to.replace(".", File.separator)

    if (fromPath != toPath) {
      val srcDirs = listOf("libs", "apps").flatMap { lib ->
        rootDir.resolve(lib).takeIf { it.exists() }?.listFiles()?.toList().orEmpty()
          .map { it.resolve("src/main/java") }
          .filter { it.exists() }
      }

      var moveCount = 0
      for (srcDir in srcDirs) {
        val fromDir = srcDir.resolve(fromPath)
        val toDir = srcDir.resolve(toPath)
        if (fromDir.exists() && fromDir.isDirectory) {
          if (!dry) {
            toDir.parentFile.mkdirs()
            fromDir.copyRecursively(toDir, overwrite = true)
            fromDir.deleteRecursively()
          }
          moveCount++
          logger.lifecycle("  directory: ${fromDir.relativeTo(rootDir)} -> ${toDir.relativeTo(rootDir)}")
        }
      }

      // Remove empty intermediate directories
      if (!dry && moveCount > 0) {
        srcDirs.forEach { srcDir ->
          val parts = fromPath.split(File.separatorChar)
          for (i in parts.indices.reversed()) {
            val dir = srcDir.resolve(parts.take(i + 1).joinToString(File.separator))
            if (dir.exists() && dir.isDirectory && dir.listFiles()?.isEmpty() != false) {
              dir.deleteRecursively()
            }
          }
        }
      }
      logger.lifecycle("Directories renamed: $moveCount")
    }

    logger.lifecycle("Done!")
  }
}
