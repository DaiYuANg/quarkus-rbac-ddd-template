plugins {
  alias(libs.plugins.quarkus)
  alias(libs.plugins.plantuml)
}

// JDK 25: JVM args for Elytron SASL Provider (DigestClientFactory) Security Provider loading
val jdk25SaslArgs = listOf(
  "--add-opens=java.base/java.security=ALL-UNNAMED",
  "--add-reads=java.base=ALL-UNNAMED"
)

tasks.quarkusDev{
  jvmArguments.addAll(jdk25SaslArgs)
}

tasks.quarkusRun{
  jvmArguments.addAll(jdk25SaslArgs)
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
  implementation(projects.libs.cache)
  implementation(projects.libs.security)

  implementation(libs.bundles.quarkus.application)
  implementation(libs.bundles.quarkus.observability)
  implementation(libs.bundles.quarkus.security.application)
  // Explicitly pull SASL digest (JDK 25 may need it on app classpath for Provider loading)
  implementation(libs.wildfly.elytron.sasl.digest)

  testImplementation(libs.bundles.quarkus.test)
}
