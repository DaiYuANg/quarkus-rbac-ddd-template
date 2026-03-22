plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(libs.quarkus.arc)
  api(libs.quarkus.rest.jackson)
  api(libs.fesod.sheet)
}
