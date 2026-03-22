plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.persistence)
  api(projects.libs.security)
  api(libs.quarkus.arc)

  annotationProcessor(enforcedPlatform(libs.quarkus.bom))
  annotationProcessor(libs.hibernate.processor)
  annotationProcessor(libs.jakarta.persistence)
  annotationProcessor(libs.jakarta.annotation)
}
