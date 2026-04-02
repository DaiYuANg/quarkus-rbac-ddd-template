plugins { alias(libs.plugins.jandex) }

dependencies {
  implementation(projects.libs.common)
  implementation(projects.libs.identity)
  implementation(projects.libs.audit)
  implementation(projects.libs.cache)
  implementation(projects.libs.security)
  implementation(projects.modules.identity)
  implementation(libs.quarkus.arc)
  implementation(libs.quarkus.rest)
  implementation(libs.quarkus.rest.jackson)
  implementation(libs.quarkus.smallrye.jwt)
  implementation(libs.quarkus.hibernate.orm.panache)
}
