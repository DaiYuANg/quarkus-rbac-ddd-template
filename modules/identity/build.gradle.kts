plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.identity)
  api(projects.libs.accesscontrol)
  api(projects.libs.audit)
  api(projects.libs.cache)
  api(projects.libs.security)
  api(libs.quarkus.arc)
  api(libs.quarkus.hibernate.orm.panache)
  api(libs.quarkus.hibernate.validator)
}
