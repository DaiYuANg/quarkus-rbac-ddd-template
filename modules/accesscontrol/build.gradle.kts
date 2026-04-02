plugins { alias(libs.plugins.jandex) }

dependencies {
  implementation(projects.libs.common)
  implementation(projects.libs.persistence)
  implementation(projects.libs.identity)
  implementation(projects.libs.accesscontrol)
  implementation(projects.libs.audit)
  implementation(projects.libs.cache)
  implementation(projects.libs.security)
  implementation(libs.quarkus.arc)
  implementation(libs.quarkus.hibernate.orm.panache)
  implementation(libs.quarkus.hibernate.validator)
}
