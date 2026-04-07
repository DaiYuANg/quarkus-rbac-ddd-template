import io.quarkus.gradle.QuarkusPlugin

plugins{
  alias(libs.plugins.quarkus)
}

subprojects {
  apply<QuarkusPlugin>()

  tasks.quarkusDev{
    workingDirectory = rootProject.projectDir
  }
}