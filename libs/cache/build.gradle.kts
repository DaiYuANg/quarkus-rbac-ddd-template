plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.security)
  api(libs.quarkus.arc)
  api(libs.quarkus.redis.client)
  api(libs.quarkus.jackson)
  api(libs.quarkus.caffeine)
  api(libs.commons.codec)
  api(libs.quarkus.rest)
}
