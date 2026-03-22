import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import java.nio.charset.StandardCharsets.UTF_8
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the

plugins {
  `java-library`
  alias(libs.plugins.quarkus) apply false
  alias(libs.plugins.jandex) apply false
  alias(libs.plugins.version.check)
  alias(libs.plugins.dotenv)
  alias(libs.plugins.lombok) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.spotbugs)
  alias(libs.plugins.owasp.dependency.check)
  alias(libs.plugins.git)
  alias(libs.plugins.task.tree)
  alias(libs.plugins.plantuml) apply false
  idea
}

group = "com.liangdian"

val rootLibs = the<VersionCatalogsExtension>().named("libs")

allprojects {
  version = "1.0.0-SNAPSHOT"

  tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }
}

subprojects {
  pluginManager.apply(JavaLibraryPlugin::class.java)
  pluginManager.apply(rootLibs.findPlugin("lombok").get().get().pluginId)
  pluginManager.apply("checkstyle")
  pluginManager.apply("jacoco")
  pluginManager.apply(rootLibs.findPlugin("spotbugs").get().get().pluginId)
  pluginManager.apply(rootLibs.findPlugin("owasp-dependency-check").get().get().pluginId)

  extensions.findByType<CheckstyleExtension>()?.apply {
    toolVersion = rootLibs.findVersion("checkstyle").get().requiredVersion
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
  }
  tasks
      .matching { it.name.startsWith("checkstyle") }
      .configureEach { tasks.findByName("jandex")?.let { dependsOn(it) } }

  spotbugs {
    reportLevel = Confidence.MEDIUM
    effort = Effort.DEFAULT
    ignoreFailures.set(true)
  }

  tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") { required.set(true) }
  }

  extensions.configure<org.gradle.testing.jacoco.plugins.JacocoPluginExtension> {
    toolVersion = "0.8.12"
  }

  extensions.configure<JavaPluginExtension> {
    toolchain {
      languageVersion = JavaLanguageVersion.of(rootLibs.findVersion("jdk").get().requiredVersion)
    }
    withSourcesJar()
  }

  dependencies {
    add("implementation", enforcedPlatform(rootLibs.findLibrary("quarkus-bom").get()))
    add("compileOnly", rootLibs.findLibrary("jetbrains-annotations").get())
    add("implementation", rootLibs.findLibrary("mapstruct").get())
    add("annotationProcessor", rootLibs.findLibrary("mapstruct-processor").get())
  }

  tasks.withType<JavaCompile>().configureEach {
    options.encoding = UTF_8.name()
    options.compilerArgs.add("-parameters")
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    extensions.configure<org.gradle.testing.jacoco.plugins.JacocoTaskExtension> { isEnabled = true }
  }

  extensions
      .findByType<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension>()
      ?.apply {
        formats.set(listOf("HTML", "JSON"))
        suppressionFile.set(rootProject.file("config/owasp/suppressions.xml").absolutePath)
      }
}

configure(
    listOf(
        projects.libs.common,
        projects.libs.persistence,
        projects.libs.accesscontrol,
        projects.libs.identity,
        projects.libs.audit,
        projects.libs.redis,
        projects.libs.export,
        projects.libs.security,
    )
) {
  pluginManager.apply(rootLibs.findPlugin("jandex").get().get().pluginId)
}

spotless {
  format("misc") {
    target("*.md", ".gitignore")
    endWithNewline()
  }
  java {
    target("**/*.java")
    importOrder()
    removeUnusedImports()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt()
  }
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}
