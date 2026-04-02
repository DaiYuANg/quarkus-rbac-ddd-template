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
  implementation(projects.libs.security)
  implementation(projects.libs.identity)
  implementation(projects.libs.accesscontrol)
  implementation(projects.modules.identity)
  implementation(projects.modules.accesscontrol)
  implementation(projects.modules.securityRuntime)
  implementation(projects.modules.exampleDdd)

  implementation(libs.bundles.quarkus.application)
  implementation(libs.bundles.quarkus.observability)
  implementation(libs.bundles.quarkus.security.application)

  testImplementation(libs.bundles.quarkus.test)
  testImplementation(projects.libs.cache)
  testImplementation(projects.libs.audit)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.jdbc)
}
