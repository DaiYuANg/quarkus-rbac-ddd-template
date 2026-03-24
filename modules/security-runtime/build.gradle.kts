plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.identity)
  api(projects.libs.audit)
  api(projects.libs.cache)
  api(projects.libs.security)
  api(projects.modules.identity)
  api(libs.quarkus.arc)
  api(libs.quarkus.rest)
  api(libs.quarkus.rest.jackson)
  api(libs.quarkus.smallrye.jwt)
  api(libs.quarkus.hibernate.orm.panache)
}
