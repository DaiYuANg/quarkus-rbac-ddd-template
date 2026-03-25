import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsPlugin
import java.nio.charset.StandardCharsets.UTF_8
import name.remal.gradle_plugins.lombok.LombokPlugin
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin

plugins {
  `java-library`
  alias(libs.plugins.quarkus) apply false
  alias(libs.plugins.jandex) apply false
  alias(libs.plugins.version.check)
  alias(libs.plugins.dotenv)
  alias(libs.plugins.lombok)
  alias(libs.plugins.spotless)
  alias(libs.plugins.spotbugs)
  alias(libs.plugins.owasp.dependency.check)
  alias(libs.plugins.git)
  alias(libs.plugins.task.tree)
  alias(libs.plugins.plantuml) apply false
  idea
}

group = "com.github.DaiYuANg"

val rootLibs = libs

tasks.register<ReplacePackageTask>("replacePackage")

tasks.register<GenerateRsaKeysTask>("generateRsaKeys")

val verifyLayerDependencies = tasks.register<VerifyLayerDependenciesTask>("verifyLayerDependencies")

tasks.named("check") { dependsOn(verifyLayerDependencies) }

allprojects {
  version = "1.0.0-SNAPSHOT"

  tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }
}

subprojects {
  apply<JavaLibraryPlugin>()
  apply<LombokPlugin>()
  apply<CheckstylePlugin>()
  apply<JacocoPlugin>()
  apply<SpotBugsPlugin>()
  apply<DependencyCheckPlugin>()

  extensions.findByType<CheckstyleExtension>()?.apply {
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

  extensions.configure<JacocoPluginExtension> { toolVersion = "0.8.14" }

  extensions.configure<JavaPluginExtension> {
    toolchain { languageVersion = JavaLanguageVersion.of(rootLibs.versions.jdk.get()) }
    withSourcesJar()
  }

  dependencies {
    implementation(enforcedPlatform(rootLibs.quarkus.bom))
    implementation(enforcedPlatform(rootLibs.quarkus.blaze.persistence.bom))
    annotationProcessor(enforcedPlatform(rootLibs.quarkus.blaze.persistence.bom))
    annotationProcessor(enforcedPlatform(rootLibs.quarkus.bom))
    compileOnly(rootLibs.jetbrains.annotations)
    implementation(rootLibs.mapstruct)
    implementation(rootLibs.guava)
    implementation(rootLibs.record.builder.core)
    annotationProcessor(rootLibs.mapstruct.processor)
    annotationProcessor(rootLibs.record.builder.processor)
  }

  tasks.withType<JavaCompile>().configureEach {
    options.encoding = UTF_8.name()
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation")
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    extensions.configure<JacocoTaskExtension> { isEnabled = true }
  }

  extensions
    .findByType<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension>()
    ?.apply {
      formats.set(listOf("HTML", "JSON"))
      suppressionFile.set(rootProject.file("config/owasp/suppressions.xml").absolutePath)
    }
}

spotless {
  format("misc") {
    target("*.md", ".gitignore")
    endWithNewline()
  }
  java {
    target("**/*.java")
    // GOOGLE style: 2-space indent (AOSP uses 4). Imports are handled by the formatter.
    googleJavaFormat(libs.versions.googleJavaFormat.get())
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}
