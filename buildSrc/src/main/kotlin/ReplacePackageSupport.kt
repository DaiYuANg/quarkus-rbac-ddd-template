import java.io.File

internal object ReplacePackageSupport {

  val contentExtensions =
    listOf("java", "kt", "kts", "yaml", "yml", "md", "sql", "xml", "properties")

  val excludedRoots = setOf("build", ".gradle", ".git", "buildSrc/build")

  fun shouldExclude(relativePath: String, excludedDirs: Set<String> = excludedRoots): Boolean =
    excludedDirs.any { relativePath == it || relativePath.startsWith("$it/") || relativePath.contains("/$it/") }

  fun shouldReplaceContent(
    rootDir: File,
    file: File,
    excludedDirs: Set<String> = excludedRoots,
    extensions: List<String> = contentExtensions,
  ): Boolean {
    if (!file.isFile) {
      return false
    }
    val relativePath = file.relativeTo(rootDir).invariantSeparatorsPath
    if (shouldExclude(relativePath, excludedDirs)) {
      return false
    }
    return extensions.any { ext -> file.extension.equals(ext, ignoreCase = true) }
  }

  fun replacePackageInContent(content: String, fromPackage: String, toPackage: String): String =
    content
      .replace(fromPackage, toPackage)
      .replace(fromPackage.replace(".", "/"), toPackage.replace(".", "/"))

  fun collectSourceRoots(rootDir: File, excludedDirs: Set<String> = excludedRoots): List<File> =
    rootDir.walkTopDown()
      .filter { it.isDirectory }
      .filter { dir ->
        val relativePath = dir.relativeTo(rootDir).invariantSeparatorsPath
        relativePath.isNotEmpty() && !shouldExclude(relativePath, excludedDirs)
      }
      .filter { dir ->
        val segments = dir.relativeTo(rootDir).invariantSeparatorsPath.split('/')
        segments.size >= 3 &&
          segments[segments.size - 3] == "src" &&
          segments.last() in setOf("java", "kotlin")
      }
      .toList()

  fun renamePackageDirectories(
    rootDir: File,
    fromPackage: String,
    toPackage: String,
    dryRun: Boolean,
    sourceRoots: List<File> = collectSourceRoots(rootDir),
    log: (String) -> Unit = {},
  ): Int {
    val fromPath = fromPackage.replace(".", File.separator)
    val toPath = toPackage.replace(".", File.separator)
    if (fromPath == toPath) {
      return 0
    }

    var moveCount = 0
    for (sourceRoot in sourceRoots) {
      val fromDir = sourceRoot.resolve(fromPath)
      if (!fromDir.exists() || !fromDir.isDirectory) {
        continue
      }
      val toDir = sourceRoot.resolve(toPath)
      if (!dryRun) {
        toDir.parentFile.mkdirs()
        fromDir.copyRecursively(toDir, overwrite = true)
        fromDir.deleteRecursively()
      }
      moveCount++
      log("  directory: ${fromDir.relativeTo(rootDir)} -> ${toDir.relativeTo(rootDir)}")
    }

    if (!dryRun && moveCount > 0) {
      cleanupEmptyPackageDirectories(sourceRoots, fromPath)
    }
    return moveCount
  }

  private fun cleanupEmptyPackageDirectories(sourceRoots: List<File>, packagePath: String) {
    for (sourceRoot in sourceRoots) {
      val packageDir = sourceRoot.resolve(packagePath)
      if (!packageDir.exists()) {
        val parts = packagePath.split(File.separatorChar)
        for (index in parts.indices.reversed()) {
          val candidate = sourceRoot.resolve(parts.take(index + 1).joinToString(File.separator))
          if (!candidate.exists()) {
            continue
          }
          val children = candidate.listFiles()
          if (candidate.isDirectory && children != null && children.isEmpty()) {
            candidate.delete()
          }
        }
      }
    }
  }
}
