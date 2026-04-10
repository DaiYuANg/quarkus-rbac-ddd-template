import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReplacePackageSupportTest {

  @Test
  fun `collectSourceRoots includes modules and test or kotlin roots`() {
    val rootDir = createTempDirectory("replace-package-roots").toFile()
    rootDir.resolve("modules/identity/src/main/java").mkdirs()
    rootDir.resolve("apps/admin-api/src/test/java").mkdirs()
    rootDir.resolve("libs/common/src/main/kotlin").mkdirs()
    rootDir.resolve("buildSrc/build/generated/src/main/java").mkdirs()

    val sourceRoots =
      ReplacePackageSupport.collectSourceRoots(rootDir)
        .map { it.relativeTo(rootDir).invariantSeparatorsPath }
        .sorted()

    assertTrue("modules/identity/src/main/java" in sourceRoots)
    assertTrue("apps/admin-api/src/test/java" in sourceRoots)
    assertTrue("libs/common/src/main/kotlin" in sourceRoots)
    assertFalse("buildSrc/build/generated/src/main/java" in sourceRoots)
  }

  @Test
  fun `renamePackageDirectories moves package trees across java kotlin and test roots`() {
    val rootDir = createTempDirectory("replace-package-move").toFile()
    val fromPackage = "com.github.DaiYuANg"
    val toPackage = "io.acme.demo"
    val fromPath = fromPackage.replace(".", "/")
    val toPath = toPackage.replace(".", "/")

    rootDir.resolve("modules/identity/src/main/java/$fromPath/service").mkdirs()
    rootDir.resolve("apps/admin-api/src/test/java/$fromPath/support").mkdirs()
    rootDir.resolve("libs/common/src/main/kotlin/$fromPath/util").mkdirs()
    rootDir.resolve("buildSrc/build/generated/src/main/java/$fromPath/ignored").mkdirs()

    val moveCount =
      ReplacePackageSupport.renamePackageDirectories(
        rootDir = rootDir,
        fromPackage = fromPackage,
        toPackage = toPackage,
        dryRun = false,
      )

    assertEquals(3, moveCount)
    assertTrue(rootDir.resolve("modules/identity/src/main/java/$toPath/service").isDirectory)
    assertTrue(rootDir.resolve("apps/admin-api/src/test/java/$toPath/support").isDirectory)
    assertTrue(rootDir.resolve("libs/common/src/main/kotlin/$toPath/util").isDirectory)
    assertTrue(rootDir.resolve("buildSrc/build/generated/src/main/java/$fromPath/ignored").isDirectory)
    assertFalse(rootDir.resolve("modules/identity/src/main/java/$fromPath").exists())
    assertFalse(rootDir.resolve("apps/admin-api/src/test/java/$fromPath").exists())
    assertFalse(rootDir.resolve("libs/common/src/main/kotlin/$fromPath").exists())
  }
}
