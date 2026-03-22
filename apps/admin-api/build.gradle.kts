plugins {
  alias(libs.plugins.quarkus)
  alias(libs.plugins.plantuml)
}

classDiagrams {
  classDiagram {
    name("DDD Application Layers")
    include(packages().withName("com.github.DaiYuANg.api"))
    include(packages().withName("com.github.DaiYuANg.application"))
    writeTo(file("build/plantuml/ddd-layers.puml"))
  }
}

dependencies {
  implementation(enforcedPlatform(libs.quarkus.bom))
  implementation(projects.libs.common)
  implementation(projects.libs.persistence)
  implementation(projects.libs.identity)
  implementation(projects.libs.accesscontrol)
  implementation(projects.libs.audit)
  implementation(projects.libs.redis)
  implementation(projects.libs.export)
  implementation(projects.libs.security)

  implementation(libs.bundles.quarkus.application)
  implementation(libs.bundles.quarkus.observability)
  implementation(libs.bundles.quarkus.security.application)

  testImplementation(libs.bundles.quarkus.test)
}
