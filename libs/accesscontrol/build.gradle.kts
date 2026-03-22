plugins { alias(libs.plugins.jandex) }

dependencies {
  api(projects.libs.persistence)
  api(libs.quarkus.arc)
  implementation(libs.blaze.persistence.quarkus)
  runtimeOnly(libs.blaze.persistence.hibernate)

  annotationProcessor(enforcedPlatform(libs.quarkus.bom))
  annotationProcessor(libs.hibernate.processor)
  annotationProcessor(libs.jakarta.persistence)
  annotationProcessor(libs.jakarta.annotation)
}
