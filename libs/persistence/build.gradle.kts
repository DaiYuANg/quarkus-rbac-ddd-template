plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.common)
  api(projects.libs.security)
  api(libs.quarkus.arc)
  api(libs.quarkus.hibernate.orm)
  api(libs.quarkus.hibernate.orm.panache)
  api(libs.quarkus.jdbc.postgresql)
  api(libs.quarkus.agroal)
  api(libs.quarkus.hibernate.validator)
  api(libs.quarkus.rest.jackson)

  implementation(libs.blaze.persistence.quarkus)
  runtimeOnly(libs.blaze.persistence.hibernate)

  annotationProcessor(libs.hibernate.processor)
  annotationProcessor(libs.jakarta.persistence)
  annotationProcessor(libs.jakarta.annotation)
}
