plugins {
  alias(libs.plugins.quarkus)
  alias(libs.plugins.plantuml)
}

classDiagrams {
  classDiagram {
    name("DDD Application Layers")
    include(packages().withName("com.github.DaiYuANg.modules"))
    writeTo(file("build/plantuml/ddd-layers.puml"))
  }
}

dependencies {
  implementation(enforcedPlatform(libs.quarkus.bom))
  implementation(projects.libs.common)
  implementation(projects.libs.restSupport)
  implementation(projects.modules.identity)
  implementation(projects.modules.accesscontrol)
  implementation(projects.modules.securityRuntime)
  implementation(projects.modules.exampleDdd)

  implementation(libs.bundles.quarkus.application)
  implementation(libs.bundles.quarkus.observability)
  implementation(libs.bundles.quarkus.security.application)

  testImplementation(libs.bundles.quarkus.test)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.jdbc)
}
