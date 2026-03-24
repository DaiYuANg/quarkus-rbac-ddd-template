plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.security)
  api(libs.quarkus.rest)
  api(libs.quarkus.rest.jackson)
  api(libs.quarkus.hibernate.validator)
  api(libs.quarkus.security)
}
