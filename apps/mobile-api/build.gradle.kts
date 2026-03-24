plugins { alias(libs.plugins.quarkus) }

dependencies {
  implementation(enforcedPlatform(libs.quarkus.bom))
  implementation(projects.libs.common)
  implementation(projects.modules.identity)
  implementation(projects.modules.securityRuntime)

  implementation(libs.bundles.quarkus.application)
  implementation(libs.bundles.quarkus.observability)
  implementation(libs.bundles.quarkus.security.application)

  testImplementation(libs.bundles.quarkus.test)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.jdbc)
}
