plugins { alias(libs.plugins.quarkus) }

dependencies {
  implementation(enforcedPlatform(libs.quarkus.bom))
  implementation(libs.bundles.quarkus.migrator)
}
