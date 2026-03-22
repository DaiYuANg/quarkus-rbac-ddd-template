plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.security)
  api(libs.quarkus.arc)
  api(libs.quarkus.redis.client)
}
