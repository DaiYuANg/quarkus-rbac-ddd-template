plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(libs.quarkus.arc)
  api(libs.quarkus.security)
  api(libs.quarkus.smallrye.jwt)
  api(libs.quarkus.smallrye.jwt.build)
  api(libs.quarkus.elytron.security.common)
  api(libs.microprofile.config)
}
